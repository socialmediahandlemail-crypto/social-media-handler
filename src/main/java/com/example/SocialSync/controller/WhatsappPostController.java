package com.example.SocialSync.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SocialSync.model.User;
import com.example.SocialSync.model.WhatsAppScheduledPost;
import com.example.SocialSync.repository.UserRepository;
import com.example.SocialSync.repository.WhatsAppScheduledPostRepository;



@RestController
@RequestMapping("/api/posts")
public class WhatsappPostController {

    private final WhatsAppScheduledPostRepository repository;
    private final UserRepository userRepository;

    public WhatsappPostController(WhatsAppScheduledPostRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }
    //Helper method 
    private String getLoggedUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object principal = authentication.getPrincipal();

        // Check if the principal is your custom User class
        if (principal instanceof com.example.SocialSync.model.User) {
            // ✅ CORRECT: Get the email directly from your User object
            return ((com.example.SocialSync.model.User) principal).getEmail();
        } 
        
        // Fallback for standard UserDetails (though your app uses the custom one above)
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        
        if (principal instanceof String) {
            return (String) principal;
        }
        
        throw new RuntimeException("Unable to identify user");
    }

    @PostMapping("/schedule")
    public WhatsAppScheduledPost scheduleNewPost(@RequestBody WhatsAppScheduledPost post) {
        String email = getLoggedUserEmail();
        System.out.println("DEBUG: Token contains email/user: " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found in database"));
        post.setUserId(user.getId());
        post.setStatus("PENDING");
        return repository.save(post);
    }

    @GetMapping("/all")
    public List<WhatsAppScheduledPost> getAllPosts() {
        String email = getLoggedUserEmail();
        User user = userRepository.findByEmail(email).orElseThrow(()-> new RuntimeException("User not foung"));

        return repository.findByUserId(user.getId());
    }
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable String id , @AuthenticationPrincipal UserDetails userDetails) {
        String email = getLoggedUserEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        repository.deleteByIdAndUserId(id,user.getId());
    }
}