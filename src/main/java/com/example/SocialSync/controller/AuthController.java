package com.example.SocialSync.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SocialSync.dto.ForgotPasswordRequest;
import com.example.SocialSync.dto.LoginRequest;
import com.example.SocialSync.dto.LoginResponse;
import com.example.SocialSync.dto.ResetPasswordRequest;
import com.example.SocialSync.dto.SignupRequest;
import com.example.SocialSync.dto.SignupResponse;
import com.example.SocialSync.service.AuthService;



@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // ===============================
    // LOGIN AND SIGNUP
    // ===============================

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest request) {
        SignupResponse response=authService.signupUser(request);
        if (!response.isSuccess()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.loginUser(request);
            // If user login, response.getToken() will be null and requiresOtp=true
            // If admin login, response.getToken() will be valid
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 🔥 NEW: Verify User Secret Key (Step 2 for Users)
    @PostMapping("/verify-key")
    public ResponseEntity<?> verifyKey(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.verifyUserSecret(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/resend-key")
    public ResponseEntity<?> resendKey(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Calls the exact same service method you already built!
            authService.generateAndSendSecretKey(email);
            
            return ResponseEntity.ok(Map.of(
                "message", "New Secret Key sent to email.", 
                "expiresIn", "2 minutes"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ===============================
    // FORGOT PASSWORD
    // ===============================

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok("Password reset email sent");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request){
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successful");
    }
}