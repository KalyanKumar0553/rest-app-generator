package com.src.main.service;

import jakarta.servlet.http.HttpServletRequest;

public interface VisitTrackingService {

    void trackHomeVisit(HttpServletRequest request);
}
