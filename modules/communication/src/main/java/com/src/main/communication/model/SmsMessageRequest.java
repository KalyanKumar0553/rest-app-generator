package com.src.main.communication.model;

public record SmsMessageRequest(String sender, String recipient, String message) {
}
