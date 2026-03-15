package com.src.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.src.main.dto.NewsletterSubscribeRequestDTO;
import com.src.main.dto.NewsletterSubscribeResponseDTO;
import com.src.main.service.NewsletterSubscriptionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/newsletter")
@RequiredArgsConstructor
public class NewsletterSubscriptionController {

    private final NewsletterSubscriptionService newsletterSubscriptionService;

    @PostMapping("/subscriptions")
    public ResponseEntity<NewsletterSubscribeResponseDTO> subscribe(@Valid @RequestBody NewsletterSubscribeRequestDTO request) {
        boolean subscribed = newsletterSubscriptionService.subscribe(request.getEmail());
        String message = subscribed
                ? "Subscribed successfully."
                : "Email already subscribed.";
        return ResponseEntity.accepted().body(new NewsletterSubscribeResponseDTO(subscribed, message));
    }
}
