package com.example.SocialSync.dto;

import lombok.Data;

@Data
public class YoutubePostRequest {
    private String title;
    private String description;
    private String privacy; 
    
    // 🔥 NEW: Generic media fields
    private String mediaUrl;  // The Cloudinary URL
    private String mediaType; // "IMAGE" or "VIDEO"
}