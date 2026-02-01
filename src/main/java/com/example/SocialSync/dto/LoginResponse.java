package com.example.SocialSync.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse{
    private String token;
    private String email;
    private String username;
    private String message;
    // 🔥 NEW: Tells frontend if flow is complete or needs step 2
    private boolean requiresOtp;
}