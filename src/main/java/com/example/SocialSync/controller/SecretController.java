package com.example.SocialSync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SocialSync.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/secret")
@CrossOrigin(origins = "*")
public class SecretController {

    @Autowired
    private AuthService authService;

    // 🔥 Maps to https://social-media-handler-backend.onrender.com/api/secret/generate
    @PostMapping("/generate")
    public ResponseEntity<?> generateSecret(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            authService.generateAndSendSecretKey(email);
            
            return ResponseEntity.ok(Map.of(
                "message", "New Secret Key sent to email.", 
                "expiresIn", "2 minutes"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}