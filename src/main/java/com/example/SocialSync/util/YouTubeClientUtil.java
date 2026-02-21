package com.example.SocialSync.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;

public class YouTubeClientUtil {
    public static YouTube createClient(String accessToken) throws Exception {
        return new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> request.getHeaders()
                        .setAuthorization("Bearer " + accessToken))
                .setApplicationName("SocialMediaHandler")
                .build();
    }
}