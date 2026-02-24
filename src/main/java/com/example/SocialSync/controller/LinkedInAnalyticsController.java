package com.example.SocialSync.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SocialSync.model.LinkedInScheduledPost;
import com.example.SocialSync.model.SocialConnection;
import com.example.SocialSync.repository.LinkedInScheduledPostRepository;
import com.example.SocialSync.repository.SocialConnectionRepository;
import com.example.SocialSync.service.LinkedInAnalyticsService;


@RestController
@RequestMapping("/linkedin/analytics")
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
@RequiredArgsConstructor
public class LinkedInAnalyticsController {

    private final LinkedInAnalyticsService analyticsService;
    private final LinkedInScheduledPostRepository postRepository;
    private final SocialConnectionRepository socialRepository; // ✅ Inject this

    @GetMapping("/{postId}")
    public ResponseEntity<String> getAnalytics(@PathVariable String postId) {

        // 1. Get the Post
        LinkedInScheduledPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 2. Find the Connection using userId from the post
        SocialConnection connection = socialRepository.findByUserIdAndPlatform(post.getUserId(), "LINKEDIN")
                .orElseThrow(() -> new RuntimeException("LinkedIn account not connected"));

        // 3. Fetch Analytics using the token
        return ResponseEntity.ok(
                analyticsService.fetchPostAnalytics(
                        connection.getAccessToken(),
                        post.getPlatformPostUrn()
                )
        );
    }
}