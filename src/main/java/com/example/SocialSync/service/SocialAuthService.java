package com.example.SocialSync.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialConnectionService socialConnectionService;

    // --- Google / YouTube ---
    @Value("${spring.security.oauth2.client.registration.google.client-id}") private String googleClientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}") private String googleClientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") private String googleRedirectUri;

    // --- Facebook ---
    @Value("${spring.security.oauth2.client.registration.facebook.client-id}") private String fbClientId;
    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}") private String fbClientSecret;
    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}") private String fbRedirectUri;

    // --- LinkedIn ---
    @Value("${linkedin.client-id}") private String linkedinClientId;
    @Value("${linkedin.client-secret}") private String linkedinClientSecret;
    @Value("${linkedin.redirect-uri}") private String linkedinRedirectUri;

    // --- Pinterest ---
    @Value("${pinterest.client-id}") private String pinterestClientId;
    @Value("${pinterest.client-secret}") private String pinterestClientSecret;
    @Value("${pinterest.redirect-uri}") private String pinterestRedirectUri;

    // --- Twitter (X) ---
    @Value("${twitter.client-id}") private String twitterClientId;
    @Value("${twitter.client-secret}") private String twitterClientSecret;
    @Value("${twitter.redirect-uri}") private String twitterRedirectUri;


    // ==========================================
    // 1. GENERATE LOGIN URLs
    // ==========================================
    public String getAuthorizationUrl(String platform) {
        if ("GOOGLE".equalsIgnoreCase(platform) || "YOUTUBE".equalsIgnoreCase(platform)) {
            return "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleClientId +
                    "&redirect_uri=" + googleRedirectUri +
                    "&response_type=code" +
                    "&scope=openid%20profile%20email%20https://www.googleapis.com/auth/youtube.upload" +
                    "&access_type=offline&prompt=consent";
        } 
        else if ("FACEBOOK".equalsIgnoreCase(platform)) {
            return "https://www.facebook.com/v12.0/dialog/oauth" +
                    "?client_id=" + fbClientId +
                    "&redirect_uri=" + fbRedirectUri +
                    "&scope=public_profile,pages_show_list,pages_manage_posts";
        }
        else if ("LINKEDIN".equalsIgnoreCase(platform)) {
            return "https://www.linkedin.com/oauth/v2/authorization" +
                    "?response_type=code" +
                    "&client_id=" + linkedinClientId +
                    "&redirect_uri=" + linkedinRedirectUri +
                    "&scope=openid%20profile%20email%20w_member_social";
        }
        else if ("PINTEREST".equalsIgnoreCase(platform)) {
            return "https://www.pinterest.com/oauth/" +
                    "?client_id=" + pinterestClientId +
                    "&redirect_uri=" + pinterestRedirectUri +
                    "&response_type=code" +
                    "&scope=boards:read,pins:read,pins:write,user_accounts:read";
        }
        else if ("TWITTER".equalsIgnoreCase(platform)) {
            return "https://twitter.com/i/oauth2/authorize?response_type=code" +
                    "&client_id=" + twitterClientId +
                    "&redirect_uri=" + twitterRedirectUri +
                    "&scope=tweet.read%20tweet.write%20users.read%20offline.access" +
                    "&code_challenge=challenge_verifier_string_must_be_long_enough_random_string" +
                    "&code_challenge_method=plain";
        }
        
        throw new RuntimeException("Platform not supported: " + platform);
    }

    // ==========================================
    // 2. EXCHANGE CODE FOR TOKEN (CALLBACK)
    // ==========================================
    public void handleCallback(String platform, String code, String userId) {
        String accessToken = null;
        String username = platform + " User"; // Default fallback username
        WebClient client = WebClient.create();

        try {
            if ("GOOGLE".equalsIgnoreCase(platform) || "YOUTUBE".equalsIgnoreCase(platform)) {
                Map tokenResponse = client.post().uri("https://oauth2.googleapis.com/token")
                        .bodyValue("code=" + code + "&client_id=" + googleClientId + "&client_secret=" + googleClientSecret + "&redirect_uri=" + googleRedirectUri + "&grant_type=authorization_code")
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .retrieve().bodyToMono(Map.class).block();
                accessToken = (String) tokenResponse.get("access_token");

                Map userProfile = client.get().uri("https://www.googleapis.com/oauth2/v2/userinfo")
                        .header("Authorization", "Bearer " + accessToken)
                        .retrieve().bodyToMono(Map.class).block();
                if(userProfile != null && userProfile.get("name") != null) username = (String) userProfile.get("name");

                socialConnectionService.connectPlatform(userId, "YOUTUBE", accessToken, username);
            } 
            else if ("FACEBOOK".equalsIgnoreCase(platform)) {
                String tokenUrl = "https://graph.facebook.com/v12.0/oauth/access_token?client_id=" + fbClientId + "&redirect_uri=" + fbRedirectUri + "&client_secret=" + fbClientSecret + "&code=" + code;
                JsonNode tokenResponse = client.get().uri(tokenUrl).retrieve().bodyToMono(JsonNode.class).block();
                accessToken = tokenResponse.get("access_token").asText();

                JsonNode userProfile = client.get().uri("https://graph.facebook.com/me?fields=id,name&access_token=" + accessToken).retrieve().bodyToMono(JsonNode.class).block();
                if(userProfile != null && userProfile.has("name")) username = userProfile.get("name").asText();

                socialConnectionService.connectPlatform(userId, "FACEBOOK", accessToken, username);
            }
            else if ("LINKEDIN".equalsIgnoreCase(platform)) {
                Map tokenResponse = client.post().uri("https://www.linkedin.com/oauth/v2/accessToken")
                        .bodyValue("grant_type=authorization_code&code=" + code + "&client_id=" + linkedinClientId + "&client_secret=" + linkedinClientSecret + "&redirect_uri=" + linkedinRedirectUri)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .retrieve().bodyToMono(Map.class).block();
                accessToken = (String) tokenResponse.get("access_token");
                
                socialConnectionService.connectPlatform(userId, "LINKEDIN", accessToken, username);
            }
            else if ("PINTEREST".equalsIgnoreCase(platform)) {
                String auth = pinterestClientId + ":" + pinterestClientSecret;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
                
                Map tokenResponse = client.post().uri("https://api.pinterest.com/v5/oauth/token")
                        .header("Authorization", "Basic " + encodedAuth)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .bodyValue("grant_type=authorization_code&code=" + code + "&redirect_uri=" + pinterestRedirectUri)
                        .retrieve().bodyToMono(Map.class).block();
                accessToken = (String) tokenResponse.get("access_token");

                socialConnectionService.connectPlatform(userId, "PINTEREST", accessToken, username);
            }
            else if ("TWITTER".equalsIgnoreCase(platform)) {
                String auth = twitterClientId + ":" + twitterClientSecret;
                String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

                Map tokenResponse = client.post().uri("https://api.twitter.com/2/oauth2/token")
                        .header("Authorization", "Basic " + encodedAuth)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .bodyValue("grant_type=authorization_code&client_id=" + twitterClientId + "&redirect_uri=" + twitterRedirectUri + "&code=" + code + "&code_verifier=challenge_verifier_string_must_be_long_enough_random_string")
                        .retrieve().bodyToMono(Map.class).block();
                accessToken = (String) tokenResponse.get("access_token");

                socialConnectionService.connectPlatform(userId, "TWITTER", accessToken, username);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to connect " + platform + ": " + e.getMessage());
            throw new RuntimeException("OAuth failed for " + platform);
        }
    }
}