package com.src.main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewsletterSubscribeResponseDTO {
    private boolean subscribed;
    private String message;
}
