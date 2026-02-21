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

            // 1. Get Logged-in User
            String email = getLoggedUserEmail();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found for email: " + email));

            // 2. ✅ FIX: Tell Cloudinary to automatically detect Image vs Video
            Map<String, Object> uploadParams = ObjectUtils.asMap("resource_type", "auto");
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            String mediaUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            
            // 🔥 Grab the type (will return "image" or "video")
            String mediaType = (String) uploadResult.get("resource_type"); 

            // 3. Save to DB
            MediaAsset asset = MediaAsset.builder()
                    .userId(user.getId())
                    .publicId(publicId)
                    .url(mediaUrl)
                    .platform(platform)
                    .mediaType(mediaType.toUpperCase()) // Save as "IMAGE" or "VIDEO"
                    .createdAt(LocalDateTime.now())
                    .build();
            
            mediaAssetRepository.save(asset);

            // 4. Response
            Map<String, String> response = new HashMap<>();
            response.put("mediaId", publicId);
            response.put("url", mediaUrl);
            response.put("mediaType", mediaType.toUpperCase());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}