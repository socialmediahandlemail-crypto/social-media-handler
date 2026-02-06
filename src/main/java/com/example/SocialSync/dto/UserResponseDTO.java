package com.example.SocialSync.dto;

import java.time.LocalDateTime;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private String id;
    private String username;
    private String email;
    private String role;
    private boolean isAdmin;
    private LocalDateTime createdAt;
    private int connectedAccountsCount; // Just show the count, not full objects

    private LocalDateTime lastActiveAt;
    private String status;
}
