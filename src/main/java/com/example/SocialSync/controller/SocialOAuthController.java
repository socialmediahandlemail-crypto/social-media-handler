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

    private String getLoggedUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = (auth.getPrincipal() instanceof User) ? 
                       ((User) auth.getPrincipal()).getEmail() : 
                       auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
    }

    @GetMapping("/connect")
    public ResponseEntity<?> getAuthUrl(@RequestParam String platform) {
        String url = socialAuthService.getAuthorizationUrl(platform);
        String userId = getLoggedUserId(); 
        String separator = url.contains("?") ? "&" : "?";
        url += separator + "state=" + userId;
        return ResponseEntity.ok(Map.of("url", url));
    }

    @GetMapping("/callback/{platform}")
    public RedirectView handleCallback(
            @PathVariable String platform,
            @RequestParam String code,
            @RequestParam String state) { 
        try {
            socialAuthService.handleCallback(platform.toUpperCase(), code, state);
            // ✅ FIX: Cleaned up the redirect URL for the live cloud frontend
            return new RedirectView("https://social-media-handler-frontend.onrender.com/connect.html?status=success");
        } catch (Exception e) {
            e.printStackTrace();
            return new RedirectView("https://social-media-handler-frontend.onrender.com/connect.html?status=error");
        }
    }
}