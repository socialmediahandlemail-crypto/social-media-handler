package com.example.SocialSync.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    private static final String ADMIN_PERMANENT_KEY = "123456789";

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // ================= SIGNUP =================
    public SignupResponse signupUser(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return new SignupResponse("Email already exists", null, null, false);
        }

        String assignedRole = "ROLE_USER";
        boolean isAdminFlag = false;
        String storedSecret = null;

        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            if (ADMIN_PERMANENT_KEY.equals(request.getSecretKey())) {
                assignedRole = "ROLE_ADMIN";
                isAdminFlag = true;
                storedSecret = ADMIN_PERMANENT_KEY;
            } else {
                return new SignupResponse("Invalid Secret Key for Admin Signup!", null, null, false);
            }
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(assignedRole)
                .isAdmin(isAdminFlag)
                .secretKey(storedSecret)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Welcome to SocialSync",
                AuthEmailTemplateUtil.signupSuccess(user.getUsername())
        );

        return new SignupResponse("Signup successful. Please Login.", user.getEmail(), null, true);
    }

    // ================= LOGIN STEP 1 =================
    public LoginResponse loginUser(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String inputRole = "ROLE_" + request.getRole().toUpperCase();
        if (!user.getRole().equals(inputRole)) {
            throw new RuntimeException("Role mismatch!");
        }

        // ===== ADMIN LOGIN =====
        if (user.isAdmin()) {

            if (request.getSecretKey() == null ||
                    !request.getSecretKey().equals(user.getSecretKey())) {
                throw new RuntimeException("Invalid Admin Secret Key!");
            }

            return generateLoginResponse(user, "Admin Login Successful", false);
        }

        // ===== USER LOGIN (Generate 6-digit OTP) =====
        else {

            String randomKey = String.format("%06d", new Random().nextInt(999999));

            user.setSecretKey(randomKey);

            // ✅ NEW: Set 2 Minute Expiry
            user.setSecretKeyExpiry(LocalDateTime.now().plusMinutes(2));

            userRepository.save(user);

            emailService.sendEmail(
                    user.getEmail(),
                    "Your Login Secret Key",
                    AuthEmailTemplateUtil.sendSecretKey(user.getUsername(), randomKey)
            );

            return new LoginResponse(
                    null,
                    user.getEmail(),
                    user.getUsername(),
                    "Secret Key sent to email. Please verify within 2 minutes.",
                    true
            );
        }
    }

    // ================= LOGIN STEP 2 (VERIFY OTP) =================
    public LoginResponse verifyUserSecret(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getSecretKey() == null) {
            throw new RuntimeException("Secret Key expired or not generated");
        }

        if (!user.getSecretKey().equals(request.getSecretKey())) {
            throw new RuntimeException("Invalid Secret Key");
        }

        // ✅ NEW: Expiry Check
        if (user.getSecretKeyExpiry() == null ||
                user.getSecretKeyExpiry().isBefore(LocalDateTime.now())) {

            user.setSecretKey(null);
            user.setSecretKeyExpiry(null);
            userRepository.save(user);

            throw new RuntimeException("Secret Key expired. Please login again.");
        }

        // Clear after successful login
        user.setSecretKey(null);
        user.setSecretKeyExpiry(null);
        userRepository.save(user);

        return generateLoginResponse(user, "User Login Successful", false);
    }

    // ================= FORGOT PASSWORD =================
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        String resetLink = "http://localhost:5500/reset-password?token=" + token;

        emailService.sendEmail(
                user.getEmail(),
                "🔐 Reset Your Password",
                PasswordEmailTemplateUtil.resetPassword(user.getUsername(), resetLink)
        );
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

    private LoginResponse generateLoginResponse(User user, String msg, boolean requiresOtp) {

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getUsername(),
                msg,
                requiresOtp
        );
    }

    // ================= RESEND / RE-GENERATE OTP =================
    public void generateAndSendSecretKey(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only generate for standard users, not Admins
        if (user.isAdmin()) {
            throw new RuntimeException("Admins use a permanent key.");
        }

        String randomKey = String.format("%06d", new Random().nextInt(999999));

        user.setSecretKey(randomKey);
        user.setSecretKeyExpiry(LocalDateTime.now().plusMinutes(2)); // ✅ Sets the 2-minute limit
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Your New Login Secret Key",
                AuthEmailTemplateUtil.sendSecretKey(user.getUsername(), randomKey)
        );
    }
}
