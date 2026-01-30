package com.example.SocialSync.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.SocialSync.model.MediaAsset;
import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.MediaAssetRepository;
import com.example.SocialSync.repository.UserRepository;

import lombok.RequiredArgsConstructor;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap; // Correct Import
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;
    private final UserRepository userRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "platform", required = false) String platform) {

        try {
            // 1. Get Logged-in User
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName(); // Extracted from JWT
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 2. Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            
            String mediaUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // 3. ✅ SAVE TO DB: Link Media to User
            MediaAsset asset = MediaAsset.builder()
                    .userId(user.getId())
                    .publicId(publicId)
                    .url(mediaUrl)
                    .platform(platform)
                    .createdAt(LocalDateTime.now())
                    .build();
            
            mediaAssetRepository.save(asset);

            // 4. Response
            Map<String, String> response = new HashMap<>();
            response.put("mediaId", publicId);
            response.put("url", mediaUrl);
            response.put("dbId", asset.getId()); // Useful reference

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}