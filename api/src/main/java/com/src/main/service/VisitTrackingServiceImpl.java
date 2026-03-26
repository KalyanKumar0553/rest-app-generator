package com.src.main.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.src.main.model.UniqueVisitEntity;
import com.src.main.repository.UniqueVisitRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class VisitTrackingServiceImpl implements VisitTrackingService {
    private final UniqueVisitRepository uniqueVisitRepository;
    @Value("${app.analytics.visit.salt:rest-app-generator-visit-salt}")
    private String visitSalt;

    @Override
    @Transactional
    public void trackHomeVisit(HttpServletRequest request) {
        String clientIp = resolveClientIp(request);
        if (clientIp.isBlank()) {
            return;
        }
        String ipHash = hashIp(clientIp);
        OffsetDateTime now = OffsetDateTime.now();
        UniqueVisitEntity visit = uniqueVisitRepository.findByIpHash(ipHash).orElseGet(() -> createVisit(ipHash, now));
        visit.setLastSeenAt(now);
        visit.setHitCount(visit.getHitCount() + 1);
        try {
            uniqueVisitRepository.save(visit);
        } catch (DataIntegrityViolationException ex) {
            UniqueVisitEntity existing = uniqueVisitRepository.findByIpHash(ipHash).orElseThrow(() -> ex);
            existing.setLastSeenAt(now);
            existing.setHitCount(existing.getHitCount() + 1);
            uniqueVisitRepository.save(existing);
        }
    }

    private UniqueVisitEntity createVisit(String ipHash, OffsetDateTime now) {
        UniqueVisitEntity visit = new UniqueVisitEntity();
        visit.setIpHash(ipHash);
        visit.setFirstSeenAt(now);
        visit.setLastSeenAt(now);
        visit.setHitCount(0);
        return visit;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            if (parts.length > 0) {
                return parts[0].trim();
            }
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null ? "" : remoteAddr.trim();
    }

    private String hashIp(String ip) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((visitSalt + ":" + ip).getBytes(StandardCharsets.UTF_8));
            return toHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public VisitTrackingServiceImpl(final UniqueVisitRepository uniqueVisitRepository) {
        this.uniqueVisitRepository = uniqueVisitRepository;
    }
}
