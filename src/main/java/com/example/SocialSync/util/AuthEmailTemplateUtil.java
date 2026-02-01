package com.example.SocialSync.util;

public class AuthEmailTemplateUtil {

    public static String signupSuccess(String username) {
        return """
                Hello %s,

                Your account has been created successfully.
                Please log in using your credentials.
                
                Regards,
                SocialSync Team
                """.formatted(username);
    }

    // 🔥 NEW: Secret Key Email Template
    public static String sendSecretKey(String username, String secretKey) {
        return """
                Hello %s,

                Here is your SECRET KEY for login:
                
                👉  %s  👈

                Use this key to complete your login process.
                
                Regards,
                SocialSync Security Team
                """.formatted(username, secretKey);
    }
    
    // ... [Existing loginAlert method] ...
    public static String loginAlert(String username) {
        return """
                Hello %s,
                A new login was detected on your account.
                If this wasn't you, reset your password immediately.
                """.formatted(username);
    }
}