package com.example.SocialSync.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.SocialSync.dto.UnifiedScheduleRequest;
import com.example.SocialSync.model.*;
import com.example.SocialSync.repository.*;

import lombok.RequiredArgsConstructor;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
public class SocialScheduleController {

    private final UserRepository userRepository;
    
    // Inject all your specific post repositories
    private final TwitterPostRepository twitterPostRepository;
    private final LinkedInScheduledPostRepository linkedInPostRepository;
    private final PinterestScheduledPostRepository pinterestPostRepository;
    private final WhatsAppScheduledPostRepository facebookPostRepository; 

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

    // ✅ 1. The Single Scheduling Endpoint
    @PostMapping("/post")
    public ResponseEntity<?> schedulePost(@RequestBody UnifiedScheduleRequest request) {
        String userId = getLoggedUserId();
        String platform = request.getPlatform().toUpperCase();

        try {
            switch (platform) {
                case "TWITTER":
                    TwitterPost twitterPost = new TwitterPost();
                    twitterPost.setUserId(userId);
                    twitterPost.setContent(request.getContent());
                    twitterPost.setScheduledTime(request.getScheduledTime());
                    twitterPost.setStatus("PENDING");
                    twitterPostRepository.save(twitterPost);
                    break;

                case "LINKEDIN":
                    LinkedInScheduledPost linkedInPost = new LinkedInScheduledPost();
                    linkedInPost.setUserId(userId);
                    linkedInPost.setContent(request.getContent());
                    linkedInPost.setMediaPath(request.getMediaUrl()); // Save Cloudinary URL
                    linkedInPost.setScheduledAt(request.getScheduledTime());
                    linkedInPost.setStatus(PostStatus.PENDING);
                    linkedInPostRepository.save(linkedInPost);
                    break;

                case "PINTEREST":
                    PinterestScheduledPost pinterestPost = new PinterestScheduledPost();
                    pinterestPost.setUserId(userId);
                    pinterestPost.setTitle(request.getTitle());
                    pinterestPost.setDescription(request.getContent());
                    pinterestPost.setImageUrl(request.getMediaUrl()); // Save Cloudinary URL
                    pinterestPost.setDestinationLink(request.getDestinationLink());
                    pinterestPost.setBoardId(request.getBoardId());
                    pinterestPost.setScheduledTime(request.getScheduledTime());
                    pinterestPost.setStatus("PENDING");
                    pinterestPostRepository.save(pinterestPost);
                    break;

                case "FACEBOOK":
                    WhatsAppScheduledPost fbPost = new WhatsAppScheduledPost();
                    fbPost.setUserId(userId);
                    fbPost.setPlatform("FACEBOOK");
                    fbPost.setContent(request.getContent());
                    fbPost.setScheduledTime(request.getScheduledTime());
                    fbPost.setStatus("PENDING");
                    facebookPostRepository.save(fbPost);
                    break;

                default:
                    return ResponseEntity.badRequest().body("Unsupported platform: " + platform);
            }

            return ResponseEntity.ok(Map.of("message", platform + " post scheduled successfully!"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to schedule post: " + e.getMessage());
        }
    }
}