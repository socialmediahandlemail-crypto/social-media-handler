package com.example.SocialSync.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import lombok.Data;

@Data
@lombok.Builder
@Document(collection = "media_assets")
public class MediaAsset {

    @Id
    private String id;
    private String userId;
    private String publicId;
    private String url;
    private String platform;
    private LocalDateTime createdAt;

}
