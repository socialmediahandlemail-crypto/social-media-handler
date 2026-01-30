package com.example.SocialSync.scheduler;

import com.example.SocialSync.repository.WhatsAppScheduledPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataCleanupScheduler {

    private final WhatsAppScheduledPostRepository postRepository;

    // ✅ Task 1: Delete Posts 10 minutes after their Scheduled Time
    // Runs every minute
    @Scheduled(fixedRate = 60000) 
    public void deleteOldPosts() {
        // Calculate time: Now minus 10 minutes
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(10);
        
        // Custom query method we will add to Repository next
        postRepository.deleteByScheduledTimeBefore(cutOffTime);
        
        System.out.println("🧹 Cleanup: Deleted posts scheduled before " + cutOffTime);
    }

    // ✅ Task 2: Delete Old Emails (If you decide to store them)
    // Runs every hour
    @Scheduled(cron = "0 0 * * * *") 
    public void deleteOldEmails() {
        // Since we aren't storing emails in DB yet, this is a placeholder.
        // If you create an EmailLog entity, you would call:
        // emailLogRepository.deleteBySentAtBefore(LocalDateTime.now().minusHours(24));
    }
}