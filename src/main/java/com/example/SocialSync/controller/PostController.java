package com.example.SocialSync.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SocialSync.service.PostService;


@RestController
@RequestMapping("/api/post")
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping("/count")
    public Long countPost(){
        return postService.countAllPosts();
    }
}
