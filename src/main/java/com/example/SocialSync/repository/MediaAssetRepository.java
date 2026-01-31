package com.example.SocialSync.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.SocialSync.model.MediaAsset;
import java.util.List;


public interface MediaAssetRepository extends MongoRepository<MediaAsset, String> {
    List<MediaAsset> findByuserId(String id);
}
