package com.example.SocialSync.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.SocialSync.model.WhatsAppScheduledPost;

@Repository
public interface WhatsAppScheduledPostRepository extends MongoRepository<WhatsAppScheduledPost, String> {
    List<WhatsAppScheduledPost> findByStatusAndScheduledTimeBefore(String status, LocalDateTime time);


    List<WhatsAppScheduledPost> findByUserId(String userId);


    void deleteByIdAndUserId(String id, String userId);
}