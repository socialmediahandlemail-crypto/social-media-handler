package com.example.SocialSync.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.SocialSync.service.TwitterService;



@RestController
@RequestMapping("/api/twitter")
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
public class TwitterController {

    @Autowired
    private TwitterService twitterService;

    // @PostMapping("/tweet")
    // public ResponseEntity<String> postTweet(
    //         @RequestBody TwitterPostRequest request) {

    //     String response =
    //             twitterService.postTweet(request.getText());

    //     return ResponseEntity.ok(response);
    // }
}
