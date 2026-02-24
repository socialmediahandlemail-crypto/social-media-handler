package com.example.SocialSync.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.UserRepository;
import com.example.SocialSync.service.SocialAuthService;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
public class SocialOAuthController {

    private final SocialAuthService socialAuthService;
    private final UserRepository userRepository;

    // Helper to get Logged In User ID
    private String getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth.getPrincipal() instanceof User) ? 
                       ((User) auth.getPrincipal()).getEmail() : 
                       auth.getName();
        
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    // ✅ 1. Get Real OAuth URL for ANY platform
    @GetMapping("/connect")
    public ResponseEntity<?> getAuthUrl(@RequestParam String platform) {
        String url = socialAuthService.getAuthorizationUrl(platform);
        
        // Append 'state' (User ID) so we know who is logging in when they come back
        String userId = getLoggedUserId(); 
        String separator = url.contains("?") ? "&" : "?";
        url += separator + "state=" + userId;
        
        return ResponseEntity.ok(Map.of("url", url));
    }

    // ✅ 2. Universal Callback Handler
    @GetMapping("/callback/{platform}")
    public RedirectView handleCallback(
            @PathVariable String platform,
            @RequestParam String code,
            @RequestParam String state) { // state contains our userId
        
        try {
            // Exchange code for token and save to unified SocialConnection table
            socialAuthService.handleCallback(platform.toUpperCase(), code, state);
            
            // Redirect back to your Frontend Dashboard on success
            return new RedirectView("https://social-media-handler-frontend.onrender.com/src/main/resources/static/frontend/main.html?status=" + platform + "_connected");
            
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("https://social-media-handler-frontend.onrender.com/src/main/resources/static/frontend/main.html?status=error");
        }
    }
}