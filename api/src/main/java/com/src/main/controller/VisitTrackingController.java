package com.src.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.src.main.service.VisitTrackingService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/analytics/visits")
public class VisitTrackingController {
    private final VisitTrackingService visitTrackingService;

    @PostMapping("/home")
    public ResponseEntity<Void> trackHomeVisit(HttpServletRequest request) {
        visitTrackingService.trackHomeVisit(request);
        return ResponseEntity.accepted().build();
    }

    public VisitTrackingController(final VisitTrackingService visitTrackingService) {
        this.visitTrackingService = visitTrackingService;
    }
}
