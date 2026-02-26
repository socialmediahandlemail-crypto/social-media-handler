package com.example.SocialSync.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.UserRepository;
import com.example.SocialSync.service.SocialConnectionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
public class SocialController {

    private final SocialConnectionService socialService;
    private final UserRepository userRepository;

    private String getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth.getPrincipal() instanceof User) ? 
                       ((User) auth.getPrincipal()).getEmail() : 
                       ((UserDetails) auth.getPrincipal()).getUsername();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // ✅ API 1: Get Dashboard Status
    @GetMapping("/status")
    public ResponseEntity<?> getConnectionStatus() {
        String userId = getLoggedUserId();
        Map<String, Boolean> status = socialService.getUserConnectionStatus(userId);
        int count = socialService.getActivePlatformCount(userId);
        return ResponseEntity.ok(Map.of("connections", status, "activeCount", count));
    }

    // ✅ API 2: Disconnect a Platform
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestParam String platform) {
        socialService.disconnectPlatform(getLoggedUserId(), platform);
        return ResponseEntity.ok("Disconnected successfully");
    }
}