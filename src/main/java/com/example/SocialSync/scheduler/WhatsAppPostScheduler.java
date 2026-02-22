package com.example.SocialSync.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.SocialSync.model.WhatsAppScheduledPost;
import com.example.SocialSync.repository.WhatsAppScheduledPostRepository;
import com.example.SocialSync.service.WhatsAppService;
import com.example.SocialSync.service.YoutubeService;

@Service
public class WhatsAppPostScheduler {

    private final WhatsAppScheduledPostRepository repository;
    private final WhatsAppService whatsappService;
    private final YoutubeService youtubeService;

    public WhatsAppPostScheduler(WhatsAppScheduledPostRepository repository, WhatsAppService whatsappService, YoutubeService youtubeService) {
        this.repository = repository;
        this.whatsappService = whatsappService;
        this.youtubeService = youtubeService;
    }

    @Scheduled(fixedRate = 60000)
    public void runAutomation() {
        List<WhatsAppScheduledPost> pendingList = repository.findByStatusAndScheduledTimeBefore("PENDING", LocalDateTime.now());

        for (WhatsAppScheduledPost post : pendingList) {
            try {
                String platform = post.getPlatform() != null ? post.getPlatform().trim().toUpperCase() : "";

                if ("WHATSAPP".equalsIgnoreCase(platform)) {
                    whatsappService.sendMessage(post.getRecipient(), post.getContent());
                    post.setStatus("SENT");
                } else if ("YOUTUBE".equalsIgnoreCase(platform)) {
                    if (post.getMediaUrl() == null || post.getMediaUrl().trim().isEmpty()) {
                        post.setStatus("FAILED");
                    } else {
                        String title = deriveYouTubeTitle(post.getContent());
                        String description = post.getContent() != null ? post.getContent() : "";
                        youtubeService.uploadVideoForUser(
                                post.getUserId(),
                                title,
                                description,
                                "public",
                                post.getMediaUrl(),
                                post.getMediaType()
                        );
                        post.setStatus("SENT");
                    }
                } else {
                    post.setStatus("FAILED");
                }
            } catch (Exception e) {
                post.setStatus("FAILED");
            }
            repository.save(post);
        }
    }

    private String deriveYouTubeTitle(String content) {
        if (content == null) {
            return "Scheduled Post";
        }
        String trimmed = content.trim().replaceAll("\\s+", " ");
        if (trimmed.isEmpty()) {
            return "Scheduled Post";
        }
        return trimmed.length() > 80 ? trimmed.substring(0, 80) : trimmed;
    }
}
