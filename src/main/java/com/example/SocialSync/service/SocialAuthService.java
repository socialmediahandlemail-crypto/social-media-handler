package com.example.SocialSync.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SocialAuthService {

    private final SocialConnectionService socialConnectionService;

    // Inject values from application.properties
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;
    
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${spring.security.oauth2.client.registration.facebook.client-id}")
    private String fbClientId;
    
    @Value("${spring.security.oauth2.client.registration.facebook.client-secret}")
    private String fbClientSecret;
    
    @Value("${spring.security.oauth2.client.registration.facebook.redirect-uri}")
    private String fbRedirectUri;

    // 1. Generate Login URL
    public String getAuthorizationUrl(String platform) {
        if ("GOOGLE".equalsIgnoreCase(platform) || "YOUTUBE".equalsIgnoreCase(platform)) {
            return "https://accounts.google.com/o/oauth2/v2/auth" +
                    "?client_id=" + googleClientId +
                    "&redirect_uri=" + googleRedirectUri +
                    "&response_type=code" +
                    "&scope=openid%20profile%20email%20https://www.googleapis.com/auth/youtube.upload" +
                    "&access_type=offline" +
                    "&prompt=consent";
        } 
        else if ("FACEBOOK".equalsIgnoreCase(platform)) {
            return "https://www.facebook.com/v12.0/dialog/oauth" +
                    "?client_id=" + fbClientId +
                    "&redirect_uri=" + fbRedirectUri +
                    "&scope=public_profile,pages_show_list,pages_manage_posts";
        }
        throw new RuntimeException("Platform not supported: " + platform);
    }

    // 2. Exchange Code for Token
    public void handleCallback(String platform, String code, String userId) {
        String accessToken = null;
        String platformUserId = null;
        String username = null;

        if ("GOOGLE".equalsIgnoreCase(platform) || "YOUTUBE".equalsIgnoreCase(platform)) {
            // A. Get Token
            WebClient client = WebClient.create();
            Map tokenResponse = client.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .bodyValue("code=" + code +
                            "&client_id=" + googleClientId +
                            "&client_secret=" + googleClientSecret +
                            "&redirect_uri=" + googleRedirectUri +
                            "&grant_type=authorization_code")
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            accessToken = (String) tokenResponse.get("access_token");

            // B. Get User Info (to find their name)
            Map userProfile = client.get()
                    .uri("https://www.googleapis.com/oauth2/v2/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            platformUserId = (String) userProfile.get("id");
            username = (String) userProfile.get("name");
            
            // Map YouTube as the platform name for Google auth
            socialConnectionService.connectPlatform(userId, "YOUTUBE", accessToken, username);
        }
        else if ("FACEBOOK".equalsIgnoreCase(platform)) {
            WebClient client = WebClient.create();
            
            // A. Get Token
            String tokenUrl = "https://graph.facebook.com/v12.0/oauth/access_token" +
                    "?client_id=" + fbClientId +
                    "&redirect_uri=" + fbRedirectUri +
                    "&client_secret=" + fbClientSecret +
                    "&code=" + code;

            JsonNode tokenResponse = client.get().uri(tokenUrl).retrieve().bodyToMono(JsonNode.class).block();
            accessToken = tokenResponse.get("access_token").asText();

            // B. Get User Info
            JsonNode userProfile = client.get()
                    .uri("https://graph.facebook.com/me?fields=id,name&access_token=" + accessToken)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            platformUserId = userProfile.get("id").asText();
            username = userProfile.get("name").asText();

            socialConnectionService.connectPlatform(userId, "FACEBOOK", accessToken, username);
        }
    }
}