package com.example.SocialSync.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.SocialSync.model.SocialConnection;
import com.example.SocialSync.model.TwitterPost;
import com.example.SocialSync.repository.SocialConnectionRepository;
import com.example.SocialSync.repository.TwitterPostRepository;
import com.example.SocialSync.service.TwitterService;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TwitterPostScheduler {

    private final TwitterPostRepository repository;
    private final TwitterService twitterService;
    private final SocialConnectionRepository socialRepository; // ✅ Inject this

    public TwitterPostScheduler(
            TwitterPostRepository repository,
            TwitterService twitterService,
            SocialConnectionRepository socialRepository) {
        this.repository = repository;
        this.twitterService = twitterService;
        this.socialRepository = socialRepository;
    }

    @Scheduled(fixedRate = 60000) // runs every 1 minute
    public void postScheduledTweets() {

        List<TwitterPost> posts = repository.findByStatusAndScheduledTimeBefore(
                        "PENDING", LocalDateTime.now());

        for (TwitterPost post : posts) {
            try {
                // ✅ 1. Fetch Token from Unified Table
                SocialConnection connection = socialRepository.findByUserIdAndPlatform(post.getUserId(), "TWITTER")
                        .orElseThrow(() -> new RuntimeException("Twitter account not connected"));

                String userAccessToken = connection.getAccessToken();

                // ✅ 2. Post using that token
                String response = twitterService.postTweet(userAccessToken, post.getContent());

                post.setStatus("POSTED");
                post.setPostedAt(LocalDateTime.now());
                
            } catch (Exception e) {
                post.setStatus("FAILED");
                System.err.println("Tweet Failed: " + e.getMessage());
            }
            repository.save(post);
        }
    }
}