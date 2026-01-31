package com.example.SocialSync.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@lombok.Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "social_connections")
public class SocialConnection {

    @Id
    private String id;

    private String userId;       // Links to your User
    private String platform;     // e.g., "FACEBOOK", "LINKEDIN", "YOUTUBE"
    private String platformUserId; // The user's ID on that platform
    private String username;     // The user's handle/name on that platform
    private String accessToken;  // ⚠️ Encrypt this in production!
    private String refreshToken; // For refreshing access when it expires
    private boolean isConnected; // true = Connected, false = Disconnected
    private LocalDateTime connectedAt;
}
