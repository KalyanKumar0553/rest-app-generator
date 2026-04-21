package com.src.main.communication.model;

public record EmailMessageRequest(String sender, String recipient, String subject, String htmlBody) {
}
