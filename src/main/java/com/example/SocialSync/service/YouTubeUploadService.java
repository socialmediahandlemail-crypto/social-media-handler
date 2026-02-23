package com.example.SocialSync.service;



import com.example.SocialSync.util.YouTubeClientUtil;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.springframework.stereotype.Service;


import java.io.File;
import java.io.FileInputStream;

@Service
public class YouTubeUploadService {

    public String uploadVideo(
            String accessToken,
            File videoFile,
            String title,
            String description,
            String privacy
    ) throws Exception {

        YouTube youtube = YouTubeClientUtil.createClient(accessToken);

        VideoSnippet snippet = new VideoSnippet();
        snippet.setTitle(title);
        snippet.setDescription(description);

        VideoStatus status = new VideoStatus();
        status.setPrivacyStatus(privacy); // public | private | unlisted

        Video videoMeta = new Video();
        videoMeta.setSnippet(snippet);
        videoMeta.setStatus(status);

        InputStreamContent media =
                new InputStreamContent(
                        "video/*",
                        new FileInputStream(videoFile)
                );
        media.setLength(videoFile.length());

       YouTube.Videos.Insert request =
        youtube.videos().insert(
                java.util.List.of("snippet", "status"),
                videoMeta,
                media
        );

        Video response = request.execute();
        return "https://www.youtube.com/watch?v=" + response.getId();
    }

}

