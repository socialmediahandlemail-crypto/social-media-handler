package com.example.SocialSync.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "twitter_posts")
@Data 
public class TwitterPost {
    @Id
    private String id;
    
    private String userId; // ✅ Changed: Link directly to the User's ID
    
    private String tweetId;
    private String content;
    private String status; // PENDING, POSTED, FAILED
    private LocalDateTime scheduledTime;
    private LocalDateTime postedAt;

    @jakarta.persistence.PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}