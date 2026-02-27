package com.src.main.service;

public interface NewsletterSubscriptionService {
    boolean subscribe(String email);
    void processPendingWelcomeEmails();
}
