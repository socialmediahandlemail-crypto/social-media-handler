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
import org.springframework.security.core.userdetails.UserDetails;
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

    //helper method
    private String getLoggedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // 1. If Principal is your Custom User object
        if (principal instanceof User) {
            return ((User) principal).getEmail(); // ✅ Correct: Returns email
        } 
        
        // 2. Fallback for standard UserDetails
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername(); // Might return username
        }
        
        // 3. Last resort
        return principal.toString();
    }



   @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "platform", required = false) String platform) {

        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("No file provided");
            }

            // 1. ✅ USE THE HELPER METHOD TO GET CORRECT EMAIL
            String email = getLoggedUserEmail();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

            // 2. Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            
            String mediaUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            // 3. Save to DB linked to User
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

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // This catches the RuntimeException and returns 500 instead of crashing silently
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}