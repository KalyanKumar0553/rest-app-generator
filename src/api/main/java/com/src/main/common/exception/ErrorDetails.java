package com.src.main.common.exception;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorDetails {
    private Date timestamp;
    private String message;
    private boolean isError;
    private int errorCode;
}
