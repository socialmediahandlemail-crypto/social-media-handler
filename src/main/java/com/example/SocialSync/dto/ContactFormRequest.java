package com.example.SocialSync.dto;

import lombok.Data;

@Data
public class ContactFormRequest {
    private String name;
    private String email;
    private String message;
    private String source; // e.g., 'faq_page'
}
