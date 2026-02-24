package com.example.SocialSync.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.SocialSync.dto.AdminResponseRequest;
import com.example.SocialSync.dto.ContactFormRequest;
import com.example.SocialSync.model.ContactQuestion;
import com.example.SocialSync.repository.ContactQuestionRepository;
import com.example.SocialSync.service.EmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
@CrossOrigin(origins = "https://social-media-handler-frontend.onrender.com")
public class ContactController {

    private final EmailService emailService;
    private final ContactQuestionRepository questionRepository;

    @PostMapping("/faq")
    public ResponseEntity<?> submitQuestion(@RequestBody ContactFormRequest request) {

        // 1. Save to Database
        ContactQuestion question = ContactQuestion.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .email(request.getEmail())
                .message(request.getMessage())
                .source(request.getSource())
                .submittedAt(LocalDateTime.now())
                .build();

        questionRepository.save(question);
        
        // 1. Send Email to Admin (Support Team)
        String adminSubject = "New Question from " + request.getName();
        String adminBody = buildAdminEmail(request);
        
        // Replace with your actual support email
        emailService.sendEmail("socialmediahandlemail@gmail.com", adminSubject, adminBody); 

        // 2. Send Auto-Reply to User
        String userSubject = "We received your question - Graphura Support";
        String userBody = "Hi " + request.getName() + ",\n\n" +
                "Thanks for reaching out! We have received your question:\n" +
                "\"" + request.getMessage() + "\"\n\n" +
                "Our team will get back to you within 24 hours.\n\n" +
                "Best,\nGraphura Team";
        
        emailService.sendEmail(request.getEmail(), userSubject, userBody);

        return ResponseEntity.ok(Map.of("message", "Question submitted successfully"));
    }

    private String buildAdminEmail(ContactFormRequest req) {
        return "New Contact Form Submission:\n\n" +
               "Name: " + req.getName() + "\n" +
               "Email: " + req.getEmail() + "\n" +
               "Source: " + req.getSource() + "\n\n" +
               "Message:\n" + req.getMessage();
    }

    // ==========================================
    // 2. ADMIN: Get All Questions (For Dashboard)
    // ==========================================
    @GetMapping("/all")
    public ResponseEntity<List<ContactQuestion>> getAllQuestions() {
        // Fetch all questions, sorted by newest first (optional logic)
        List<ContactQuestion> questions = questionRepository.findAll();
        // You might want to sort them here or in the repository
        questions.sort((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt())); 
        return ResponseEntity.ok(questions);
    }

    // ==========================================
    // 3. ADMIN: Respond to Question
    // ==========================================
    @PostMapping("/respond")
    public ResponseEntity<?> respondToQuestion(@RequestBody AdminResponseRequest request) {
        
        // A. Find the question in DB
        ContactQuestion question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question not found"));

        // B. Update Database
        question.setResponse(request.getResponseText());
        question.setStatus("ANSWERED");
        question.setRespondedAt(LocalDateTime.now());
        questionRepository.save(question);

        // C. Send Email to User
        String subject = "Response to your question - Graphura Support";
        String body = buildResponseEmail(question.getName(), question.getMessage(), request.getResponseText());
        
        emailService.sendEmail(question.getEmail(), subject, body);

        return ResponseEntity.ok(Map.of("message", "Response sent and saved successfully"));
    }

    // Helper to format the email
    private String buildResponseEmail(String userName, String originalQuestion, String adminResponse) {
        return "Hello " + userName + ",\n\n" +
               "You asked:\n\"" + originalQuestion + "\"\n\n" +
               "------------------------------------------------\n\n" +
               "Our Response:\n" + adminResponse + "\n\n" +
               "------------------------------------------------\n" +
               "If you have further questions, feel free to reply.\n\n" +
               "Best regards,\n" +
               "Graphura Support Team";
    }

}
