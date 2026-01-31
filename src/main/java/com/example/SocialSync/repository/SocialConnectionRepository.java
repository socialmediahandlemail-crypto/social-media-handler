package com.example.SocialSync.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.SocialSync.model.SocialConnection;

public interface SocialConnectionRepository extends MongoRepository<SocialConnection,String> {

    Optional<SocialConnection> findByUserIdAndPlatform(String userId, String platform);
    
    // Get all connected platforms for a user (for the counter)
    List<SocialConnection> findByUserIdAndIsConnectedTrue(String userId);
    
    // Check if connected
    boolean existsByUserIdAndPlatformAndIsConnectedTrue(String userId, String platform);

}
