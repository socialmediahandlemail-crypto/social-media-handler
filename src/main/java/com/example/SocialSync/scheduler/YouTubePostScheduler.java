package com.example.SocialSync.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.SocialSync.model.PostStatus;
import com.example.SocialSync.model.YouTubeAccount;
import com.example.SocialSync.model.YouTubeScheduledPost;
import com.example.SocialSync.repository.YouTubeAccountRepository;
import com.example.SocialSync.repository.YouTubeScheduledPostRepository;
import com.example.SocialSync.service.YouTubeUploadService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class YouTubePostScheduler {

    private final YouTubeScheduledPostRepository postRepository;
    private final YouTubeAccountRepository accountRepository;
    private final YouTubeUploadService uploadService;
    private final PostStatusNotifier postStatusNotifier; // ✅ ADD THIS

    @Scheduled(cron = "0 * * * * ?") // every minute
    public void autoPublish() {

        List<YouTubeScheduledPost> posts =
                postRepository.findByPlatformAndStatusAndScheduledAtLessThanEqual(
                        "YOUTUBE",
                        PostStatus.PENDING,
                        LocalDateTime.now()
                );

        for (YouTubeScheduledPost post : posts) {

            YouTubeAccount account = null;
            try {
                post.setStatus(PostStatus.PROCESSING);
                postRepository.save(post);

                account = accountRepository.findAll().get(0);

                String videoUrl = uploadService.uploadVideo(
                        account.getAccessToken(),
                        new File(post.getMediaPath()),
                        post.getTitle(),
                        post.getDescription(),
                        "public"
                );

                post.setStatus(PostStatus.POSTED);
                post.setPostedAt(LocalDateTime.now());
                post.setPlatformPostUrl(videoUrl);
                postRepository.save(post);

                // ✅ EMAIL ON SUCCESS
                postStatusNotifier.notifyPostSuccess(
                        account.getUser(),
                        "YouTube",
                        post.getPostedAt().toString()
                );


            } catch (Exception e) {

                post.setStatus(PostStatus.FAILED);
                post.setFailureReason(e.getMessage());
                postRepository.save(post);

                log.error("YouTube post failed", e);

                // ❌ EMAIL ON FAILURE
                if (account != null) {
                    postStatusNotifier.notifyPostFailure(
                            account.getUser(),
                            "YouTube",
                            e.getMessage()
                    );
                }
            }
        }
    }
}
