package com.example.SocialSync.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.SocialSync.dto.UserResponseDTO;
import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public long countOnlyUsers() {
        return userRepository.countByRole("ROLE_USER");
    }

    public List<UserResponseDTO> getAllUsers(){
        List<User> users = userRepository.findByRole("ROLE_USER");
        return users.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // ✅ OPTIMIZED: Return Single DTO
    public UserResponseDTO getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        return mapToDTO(user);
    }

    // Helper method to convert User -> UserResponseDTO
    private UserResponseDTO mapToDTO(User user) {
        // 1. Calculate Status
        String status = "Inactive";
        if (user.getLastActiveAt() != null) {
            // If last active time is AFTER (Now - 5 minutes), they are Online
            if (user.getLastActiveAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
                status = "Active";
            }
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .isAdmin(user.isAdmin())
                .createdAt(user.getCreatedAt())
                .connectedAccountsCount(user.getYoutubeAccounts() != null ? user.getYoutubeAccounts().size() : 0)
                // 🔥 Set the new fields
                .lastActiveAt(user.getLastActiveAt())
                .status(status)
                .build();
    }
}
