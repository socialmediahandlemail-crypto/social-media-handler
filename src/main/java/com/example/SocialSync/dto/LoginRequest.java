package com.example.SocialSync.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
    // 🔥 NEW: User selects "Admin" or "User" in dropdown
    private String role; 
    
    // 🔥 NEW: Required for Admin login immediately, or User verification step
    private String secretKey;
}