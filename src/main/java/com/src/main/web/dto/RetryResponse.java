package com.src.main.web.dto;
import lombok.*; @Data @NoArgsConstructor @AllArgsConstructor public class RetryResponse { private Long executionId; private String status; }