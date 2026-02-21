package com.example.SocialSync.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.SocialSync.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TwitterAuthService {

    @Value("${twitter.client-id}")
    private String clientId;

    @Value("${twitter.client-secret}")
    private String clientSecret;

    @Value("${twitter.redirect-uri}")
    private String redirectUri;

    private final SocialConnectionService socialConnectionService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // In a real app, store this mapping (userId -> codeVerifier) in Redis/Cache
    // For now, we use a static hardcoded verifier for simplicity (Works for testing)
    private static final String HARDCODED_VERIFIER = "challenge_verifier_string_must_be_long_enough_random_string";

    public String getAuthorizationUrl(String userId) {
        // PKCE Challenge generation (S256)
        String codeChallenge = generateCodeChallenge(HARDCODED_VERIFIER);

        return "https://twitter.com/i/oauth2/authorize?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&scope=tweet.read%20tweet.write%20users.read%20offline.access" +
                "&state=" + userId +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256";
    }

    public void handleCallback(String code, String userId) {
        // 1. Exchange Code for Token
        String accessToken = exchangeCodeForToken(code);

        // 2. Fetch Twitter Username (Optional, but good for UI)
        String username = "Twitter User"; // In a real app, make a quick GET to Twitter's /users/me endpoint here

        // 3. ✅ Save to the UNIFIED SocialConnection table!
        socialConnectionService.connectPlatform(userId, "TWITTER", accessToken, username);
    }

    private String exchangeCodeForToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        // Basic Auth header required for Twitter Confidential Client
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("client_id", clientId);
        body.add("redirect_uri", redirectUri);
        body.add("code_verifier", HARDCODED_VERIFIER); // Must match what we sent in step 1

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.twitter.com/2/oauth2/token",
                    request,
                    String.class
            );
            JsonNode json = objectMapper.readTree(response.getBody());
            return json.get("access_token").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange Twitter token: " + e.getMessage());
        }
    }

    // Helper to generate S256 Challenge
    private String generateCodeChallenge(String verifier) {
        try {
            byte[] bytes = verifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not supported");
        }
    }
}