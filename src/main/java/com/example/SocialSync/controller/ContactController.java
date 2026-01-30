package com.example.SocialSync.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SocialSync.dto.ContactFormRequest;
import com.example.SocialSync.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final EmailService emailService;

    @PostMapping("/faq")
    public ResponseEntity<?> submitQuestion(@RequestBody ContactFormRequest request) {
        
        // 1. Send Email to Admin (Support Team)
        String adminSubject = "New Question from " + request.getName();
        String adminBody = buildAdminEmail(request);
        
        // Replace with your actual support email
        emailService.sendEmail("ayushdby902@gmail.com", adminSubject, adminBody); 

        // 2. Send Auto-Reply to User
        String userSubject = "We received your question - Graphura Support";
        String userBody = "Hi " + request.getName() + ",\n\n" +
                "Thanks for reaching out! We have received your question:\n" +
                "\"" + request.getMessage() + "\"\n\n" +
                "Our team will get back to you within 24 hours.\n\n" +
                "Best,\nGraphura Team";
        
        emailService.sendEmail(request.getEmail(), userSubject, userBody);

        return ResponseEntity.ok(Map.of("message", "Question submitted successfully"));
    }

    private String buildAdminEmail(ContactFormRequest req) {
        return "New Contact Form Submission:\n\n" +
               "Name: " + req.getName() + "\n" +
               "Email: " + req.getEmail() + "\n" +
               "Source: " + req.getSource() + "\n\n" +
               "Message:\n" + req.getMessage();
    }

}
