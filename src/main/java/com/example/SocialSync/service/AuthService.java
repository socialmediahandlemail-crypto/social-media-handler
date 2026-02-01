package com.example.SocialSync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SocialSync.dto.LoginRequest;
import com.example.SocialSync.dto.LoginResponse;
import com.example.SocialSync.dto.SignupRequest;
import com.example.SocialSync.dto.SignupResponse;
import com.example.SocialSync.model.User;
import com.example.SocialSync.repository.UserRepository;
import com.example.SocialSync.util.AuthEmailTemplateUtil;
import com.example.SocialSync.util.JwtUtil;
import com.example.SocialSync.util.PasswordEmailTemplateUtil;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EmailService emailService;
    
    // Permanent Secret Key for Admin (As per requirement)
    private static final String ADMIN_PERMANENT_KEY = "123456789";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

   public SignupResponse signupUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return new SignupResponse("Email already exists", null, null, false);
        }

        String assignedRole = "ROLE_USER";
        boolean isAdminFlag = false;
        String storedSecret = null;

        // Logic: Role determination
        if ("ADMIN".equalsIgnoreCase(request.getRole())) { // Check if user selected Admin role
            // Verify if they provided the correct permanent key to register as Admin
            if (ADMIN_PERMANENT_KEY.equals(request.getSecretKey())) {
                assignedRole = "ROLE_ADMIN";
                isAdminFlag = true;
                storedSecret = ADMIN_PERMANENT_KEY; // Admin stores permanent key
            } else {
                return new SignupResponse("Invalid Secret Key for Admin Signup!", null, null, false);
            }
        } else {
            // User role: No secret key stored initially
            storedSecret = null; 
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .isAdmin(isAdminFlag)
                .secretKey(storedSecret) // Store key based on role
                .createdAt(LocalDateTime.now())
                .build();

        try {
            userRepository.save(user);
            // We do NOT return a token immediately for anyone to force Login flow
            // Or return token if you want auto-login (skipped here to follow strict login flow)
            
            // Send Welcome Email
            try {
                emailService.sendEmail(user.getEmail(), "Welcome to SocialSync", 
                        AuthEmailTemplateUtil.signupSuccess(user.getUsername()));
            } catch (Exception e) {
                System.out.println("Email error: " + e.getMessage());
            }
            
            return new SignupResponse("Signup successful. Please Login.", user.getEmail(), null, true);
        } catch (Exception e) {
            return new SignupResponse("Database Error: " + e.getMessage(), null, null, false);
        }
    }


    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Check if the User is trying to login with the correct registered Role
        String inputRole = "ROLE_" + request.getRole().toUpperCase(); 
        if (!user.getRole().equals(inputRole)) {
            // ✅ DEBUG INFO: Use this exception message to verify what's happening
            throw new RuntimeException("Role mismatch! DB says: " + user.getRole() + " but you sent: " + inputRole);
        }

        // --- ADMIN FLOW ---
        if (user.isAdmin()) {
            // Check Permanent Key
            if (request.getSecretKey() == null || !request.getSecretKey().equals(user.getSecretKey())) {
                throw new RuntimeException("Invalid Admin Secret Key!");
            }
            // All matches: Generate Token
            return generateLoginResponse(user, "Admin Login Successful", false);
        } 
        
        // --- USER FLOW (Random Key Generation) ---
        else {
            // 1. Generate Random 6-digit Secret Key
            String randomKey = String.format("%06d", new Random().nextInt(999999));
            
            // 2. Store in DB
            user.setSecretKey(randomKey);
            userRepository.save(user);
            
            // 3. Send Email
            emailService.sendEmail(
                user.getEmail(), 
                "Your Login Secret Key", 
                AuthEmailTemplateUtil.sendSecretKey(user.getUsername(), randomKey)
            );
            
            // 4. Return "Requires OTP" response (Token is null)
            return new LoginResponse(null, user.getEmail(), user.getUsername(), 
                    "Secret Key sent to email. Please verify.", true);
        }
    }

    // ================= VERIFY USER KEY (STEP 2) =================
    public LoginResponse verifyUserSecret(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify the Random Key stored in DB
        if (user.getSecretKey() == null || !user.getSecretKey().equals(request.getSecretKey())) {
            throw new RuntimeException("Invalid or Expired Secret Key");
        }

        // Clear secret key after successful use (Optional security measure)
        user.setSecretKey(null);
        userRepository.save(user);

        return generateLoginResponse(user, "User Login Successful", false);
    }

    // Helper to generate final success response
    private LoginResponse generateLoginResponse(User user, String msg, boolean requiresOtp) {
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new LoginResponse(token, user.getEmail(), user.getUsername(), msg, requiresOtp);
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "🔐 Reset Your Password",
                PasswordEmailTemplateUtil.resetPassword(user.getUsername(), resetLink));
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}