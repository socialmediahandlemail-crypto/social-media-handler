package com.example.SocialSync.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import com.example.SocialSync.service.WhatsAppAuthService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/oauth/whatsapp")
@CrossOrigin(origins = "*")
public class WhatsAppOAuthController {

    private final WhatsAppAuthService authService;

    public WhatsAppOAuthController(WhatsAppAuthService authService) {
        this.authService = authService;
    }

    // STEP 1: Frontend asks for Facebook/WhatsApp Login URL
@GetMapping("/connect")
public void connect(HttpServletResponse response) throws IOException {
    String redirectUri = URLEncoder.encode(
            "http://localhost:8082/oauth/whatsapp/callback",
            StandardCharsets.UTF_8
    );
    String authUrl =
            "https://www.facebook.com/v18.0/dialog/oauth" +
            "?client_id=1272276504709251" +
            "&redirect_uri=" + redirectUri +
            "&state=whatsapp_auth" +
            "&scope=whatsapp_business_management,whatsapp_business_messaging";

    response.sendRedirect(authUrl);
}

    // STEP 2: Facebook redirects back here
    @GetMapping("/callback")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String userId,
            HttpServletResponse response
    ) throws IOException {
        
        authService.handleCallback(code, userId);

        // Redirect back to frontend dashboard
        response.sendRedirect("http://127.0.0.1:5500/src/main/resources/static/frontend/main.html?status=whatsapp_connected");
    }
}