package com.example.SocialSync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    // This MUST be the email address you verified in your Brevo account
    @Value("${brevo.sender.email}")
    private String senderEmail;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String to, String subject, String htmlContent) {
        String url = "https://api.brevo.com/v3/smtp/email";

        // 1. Set up Headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // 2. Build the exact JSON body Brevo expects
        Map<String, Object> body = Map.of(
                "sender", Map.of("name", "SocialSync", "email", senderEmail),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        // 3. Send the HTTP POST Request
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("✅ Brevo Email sent successfully to: " + to);
            }
        } catch (Exception e) {
            System.err.println("❌ Brevo Email failed: " + e.getMessage());
            // You can optionally throw an exception here if you want the login to fail when emails fail
            throw new RuntimeException("Could not send email. Please try again later.");
        }
    }
}