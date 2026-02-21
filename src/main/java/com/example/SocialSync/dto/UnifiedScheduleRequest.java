package com.example.SocialSync.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UnifiedScheduleRequest {
    private String platform; // "TWITTER", "LINKEDIN", "PINTEREST", "FACEBOOK"
    private String content;  // The text of the post
    private String mediaUrl; // Optional: Cloudinary URL for images/videos
    private LocalDateTime scheduledTime; 
    
    // Pinterest specific (Optional for other platforms)
    private String title;
    private String boardId;
    private String destinationLink;
}