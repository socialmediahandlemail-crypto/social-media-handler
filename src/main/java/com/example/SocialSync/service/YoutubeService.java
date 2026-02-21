package com.example.SocialSync.service;

import com.example.SocialSync.model.SocialConnection;
import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.SocialConnectionRepository;
import com.example.SocialSync.util.YouTubeClientUtil;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class YoutubeService {

    private final SocialConnectionRepository socialConnectionRepository;

    public String uploadVideo(String title, String description, String privacyStatus, String mediaUrl, String mediaType) throws Exception {
        
        // 🚨 SAFETY CHECK: YouTube strictly forbids image uploads
        if ("IMAGE".equalsIgnoreCase(mediaType)) {
            throw new RuntimeException("YouTube API strictly requires a video file. Static images cannot be uploaded.");
        }

        // 1. Get Logged-in User
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // 2. Fetch YouTube Access Token from DB
        SocialConnection connection = socialConnectionRepository.findByUserIdAndPlatform(user.getId(), "YOUTUBE")
                .orElseThrow(() -> new RuntimeException("YouTube account not connected!"));

        String accessToken = connection.getAccessToken();

        // 3. Create YouTube Client
        YouTube youtube = YouTubeClientUtil.createClient(accessToken);

        // 4. Set Metadata
        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);
        snippet.setTags(Collections.singletonList("SocialSync"));

        // 5. Set Privacy Status
        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(privacyStatus != null ? privacyStatus : "private"); 

        Video video = new Video();
        video.setSnippet(snippet);
        video.setStatus(status);

        // 6. Connect to Cloudinary URL and Stream Data
        URL url = new URL(mediaUrl);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");
        httpConn.connect();

        if (httpConn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Failed to fetch media from Cloudinary. HTTP Status: " + httpConn.getResponseCode());
        }

        long contentLength = httpConn.getContentLengthLong();

        try (InputStream inputStream = httpConn.getInputStream()) {
            
            InputStreamContent mediaContent = new InputStreamContent("video/*", inputStream);
            if (contentLength > 0) {
                mediaContent.setLength(contentLength);
            }

            // 7. Execute Upload to YouTube
            YouTube.Videos.Insert request = youtube.videos()
                    .insert(Collections.singletonList("snippet,status"), video, mediaContent);
            
            request.getMediaHttpUploader().setDirectUploadEnabled(false);

            Video response = request.execute();
            
            return "https://www.youtube.com/watch?v=" + response.getId();
        } finally {
            httpConn.disconnect();
        }
    }
}