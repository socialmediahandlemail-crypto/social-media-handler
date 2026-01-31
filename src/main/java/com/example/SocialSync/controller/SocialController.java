package com.example.SocialSync.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.UserRepository;
import com.example.SocialSync.service.SocialAuthService;
import com.example.SocialSync.service.SocialConnectionService;
import com.example.SocialSync.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/channels")
@RequiredArgsConstructor
public class SocialController {

    private final SocialConnectionService socialService;
    private final SocialAuthService socialAuthService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    private String getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (auth.getPrincipal() instanceof User) {
            email = ((User) auth.getPrincipal()).getEmail();
        } else if (auth.getPrincipal() instanceof UserDetails) {
            email = ((UserDetails) auth.getPrincipal()).getUsername();
        } else {
            email = auth.getPrincipal().toString();
        }
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // ✅ API 1: Get Dashboard Status (Which cards are connected?)
    @GetMapping("/status")
    public ResponseEntity<?> getConnectionStatus() {
        String userId = getLoggedUserId();
        Map<String, Boolean> status = socialService.getUserConnectionStatus(userId);
        int count = socialService.getActivePlatformCount(userId);
        
        return ResponseEntity.ok(Map.of(
            "connections", status,
            "activeCount", count
        ));
    }

    // ✅ API 2: Disconnect a Platform
    @PostMapping("/disconnect")
    public ResponseEntity<?> disconnect(@RequestParam String platform) {
        socialService.disconnectPlatform(getLoggedUserId(), platform);
        return ResponseEntity.ok("Disconnected successfully");
    }

    // ✅ API 3: Simulate Connection (FOR TESTING ONLY - Real OAuth needs Redirects)
    @PostMapping("/mock-connect")
    public ResponseEntity<?> mockConnect(@RequestParam String platform) {
        // In real life, this happens after OAuth callback
        socialService.connectPlatform(getLoggedUserId(), platform, "mock_token_123", "User_" + platform);
        return ResponseEntity.ok("Connected (Mock) successfully");
    }

    // ✅ NEW: 1. Get Real OAuth URL
    @GetMapping("/auth-url")
    public ResponseEntity<?> getAuthUrl(@RequestParam String platform) {
        String url = socialAuthService.getAuthorizationUrl(platform);
        
        // Critical: Append our JWT token as "state" so we know who is connecting
        String userId = getLoggedUserId(); 
        String state = userId; // In production, encrypt this!
        
        url += "&state=" + state;
        
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ✅ NEW: 2. Callback Handler (Google/Facebook redirects here)
    @GetMapping("/callback/{platform}")
    public RedirectView handleCallback(
            @PathVariable String platform,
            @RequestParam String code,
            @RequestParam String state) { // state contains userId
        
        try {
            // The 'state' parameter is the userId we sent earlier
            String userId = state; 
            
            // Exchange code for token
            socialAuthService.handleCallback(platform, code, userId);
            
            // Redirect back to your Frontend Dashboard
            return new RedirectView("http://127.0.0.1:5500/src/main/resources/static/frontend/connect.html?status=success");
            
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("http://127.0.0.1:5500/dashboard.html?status=error");
        }
    }

}
