package com.example.SocialSync.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "scheduled_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouTubeScheduledPost {
@Id
private String id;
private String platform;
private String title;
private String description;
private String mediaPath;
private LocalDateTime scheduledAt;
private PostStatus status;
private String failureReason;
private LocalDateTime createdAt;
private LocalDateTime postedAt;
private String platformPostUrl;
@jakarta.persistence.PrePersist
public void onCreate() {
createdAt = LocalDateTime.now();
if (status == null) status = PostStatus.PENDING;
if (id == null) {
id = UUID.randomUUID().toString();
}
}
}
