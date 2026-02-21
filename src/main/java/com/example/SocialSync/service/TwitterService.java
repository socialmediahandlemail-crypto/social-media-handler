package com.example.SocialSync.service;

import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

@Service
public class TwitterService {

    private final RestTemplate restTemplate = new RestTemplate();

    // ✅ Only one method needed. It takes the specific user's token.
    public String postTweet(String accessToken, String text) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken); 
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Escape quotes to prevent JSON parsing errors
        String safeText = text.replace("\"", "\\\"");
        String body = "{ \"text\": \"" + safeText + "\" }";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.twitter.com/2/tweets",
                    request,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to post to Twitter: " + e.getMessage());
        }
    }
}