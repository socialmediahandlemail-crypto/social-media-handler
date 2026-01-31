package com.example.SocialSync.service;

import java.time.LocalDateTime;
import java.util.HashMap;

import org.springframework.stereotype.Service;

import com.example.SocialSync.model.SocialConnection;
import com.example.SocialSync.repository.SocialConnectionRepository;

import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialConnectionService {
    private final SocialConnectionRepository repository;

    public Map<String, Boolean> getUserConnectionStatus(String userId) {
        List<SocialConnection> connections = repository.findByUserIdAndIsConnectedTrue(userId);
        
        Map<String, Boolean> statusMap = new HashMap<>();
        // Default all to false
        statusMap.put("FACEBOOK", false);
        statusMap.put("WHATSAPP", false);
        statusMap.put("YOUTUBE", false);
        statusMap.put("LINKEDIN", false);
        statusMap.put("TWITTER", false);
        statusMap.put("PINTEREST", false);

        // Update connected ones to true
        for (SocialConnection conn : connections) {
            statusMap.put(conn.getPlatform(), true);
        }
        return statusMap;
    }

    public int getActivePlatformCount(String userId) {
        return repository.findByUserIdAndIsConnectedTrue(userId).size();
    }

    public void connectPlatform(String userId, String platform, String accessToken, String username) {
        SocialConnection connection = repository.findByUserIdAndPlatform(userId, platform)
                .orElse(SocialConnection.builder()
                        .userId(userId)
                        .platform(platform)
                        .build());

        connection.setAccessToken(accessToken);
        connection.setUsername(username);
        connection.setConnected(true);
        connection.setConnectedAt(LocalDateTime.now());

        repository.save(connection);
    }

    public void disconnectPlatform(String userId, String platform) {
        SocialConnection connection = repository.findByUserIdAndPlatform(userId, platform)
                .orElseThrow(() -> new RuntimeException("Connection not found"));

        connection.setConnected(false);
        connection.setAccessToken(null); // Remove token for security
        repository.save(connection);
    }
}
