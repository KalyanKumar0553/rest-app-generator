package com.src.main.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String,Object>> onConstraintViolation(ConstraintViolationException ex){
    Map<String,Object> body = new HashMap<>();
    String firstError = ex.getConstraintViolations()
            .stream()
            .findFirst()
            .map(v -> v.getPropertyPath() + " " + v.getMessage())
            .orElse("Validation failed");
    body.put("errorCode", HttpStatus.BAD_REQUEST.value());
    body.put("errorMsg", firstError);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String,Object>> onMethodArgInvalid(MethodArgumentNotValidException ex){
    Map<String,Object> body = new HashMap<>();
    String firstError = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(fe -> fe.getDefaultMessage())
            .orElse("Validation failed");
    body.put("errorCode", HttpStatus.BAD_REQUEST.value());
    body.put("errorMsg", firstError);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
  
  @ExceptionHandler(GenericException.class)
  public ResponseEntity<Map<String,Object>> onError(GenericException ex){
    Map<String,Object> body = new HashMap<>();
    body.put("error", "INTERNAL_ERROR");
    body.put("errorCode", ex.getStatus().value());
    body.put("errorMsg", ex.getErrorMsg());
    ex.printStackTrace();
    return ResponseEntity.status(ex.getStatus()).body(body);
  }
  
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String,Object>> onError(Exception ex){
    Map<String,Object> body = new HashMap<>();
    body.put("error", "INTERNAL_ERROR");
    body.put("errorMsg", ex.getMessage());
    ex.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String,Object>> onIllegalArgument(IllegalArgumentException ex){
    Map<String,Object> body = new HashMap<>();
    body.put("error", "BAD_REQUEST");
    body.put("message", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
  }
}
