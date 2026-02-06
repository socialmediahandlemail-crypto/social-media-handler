package com.example.SocialSync.config;

import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ActivityTrackingFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Check if user is logged in and not anonymous
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            
            // 2. Optimization: Only update DB if last update was > 2 minutes ago
            // This prevents hitting the database on every single click/request
            if (user.getLastActiveAt() == null || 
                user.getLastActiveAt().isBefore(LocalDateTime.now().minusMinutes(2))) {
                
                user.setLastActiveAt(LocalDateTime.now());
                userRepository.save(user);
            }
        }

        chain.doFilter(request, response);
}   
}
