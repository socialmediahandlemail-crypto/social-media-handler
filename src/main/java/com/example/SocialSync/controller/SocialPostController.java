package com.example.SocialSync.controller;

import com.example.SocialSync.dto.YoutubePostRequest;
import com.example.SocialSync.service.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
public class SocialPostController {

    private final YoutubeService youtubeService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(@RequestBody YoutubePostRequest request) {
        try {
            String videoUrl = youtubeService.uploadVideo(
                    request.getTitle(), 
                    request.getDescription(), 
                    request.getPrivacy(), 
                    request.getMediaUrl(),   // Changed parameter
                    request.getMediaType()   // New parameter
            );
            return ResponseEntity.ok("Video uploaded successfully! URL: " + videoUrl);
        } catch (RuntimeException e) {
            // Catch our custom validation error (like the IMAGE block)
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }
}