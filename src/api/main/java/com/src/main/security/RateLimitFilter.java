package com.src.main.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class RateLimitFilter extends OncePerRequestFilter {

	private final Map<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();
	private final AntPathMatcher antPathMatcher = new AntPathMatcher();
	private final AtomicLong lastCleanupEpochMs = new AtomicLong(0L);

	@Value("${app.ratelimit.enabled:true}")
	private boolean enabled;

	@Value("${app.ratelimit.max-requests:30}")
	private int maxRequests;

	@Value("${app.ratelimit.window-seconds:60}")
	private long windowSeconds;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!enabled) {
			return true;
		}

		String uri = request.getRequestURI();
		String method = request.getMethod();

		return !(
				(HttpMethod.POST.matches(method) && antPathMatcher.match("/api/projects", uri))
						|| (HttpMethod.POST.matches(method) && antPathMatcher.match("/api/projects/*/generate", uri))
						|| (HttpMethod.POST.matches(method) && antPathMatcher.match("/api/projects/*/save-and-generate", uri))
						|| (HttpMethod.PUT.matches(method) && antPathMatcher.match("/api/projects/*/spec", uri))
		);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		long now = Instant.now().toEpochMilli();
		long windowMs = windowSeconds * 1000L;
		String clientIp = resolveClientIp(request);
		String key = clientIp + "|" + request.getRequestURI();

		if (!allowRequest(key, now, windowMs)) {
			response.setStatus(429);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding(StandardCharsets.UTF_8.name());
			response.getWriter().write("{\"message\":\"Too many requests. Please try again later.\"}");
			return;
		}

		filterChain.doFilter(request, response);
	}

	private boolean allowRequest(String key, long now, long windowMs) {
		Deque<Long> timestamps = requestLog.computeIfAbsent(key, ignored -> new ConcurrentLinkedDeque<>());
		synchronized (timestamps) {
			evictOldEntries(timestamps, now, windowMs);
			if (timestamps.size() >= maxRequests) {
				return false;
			}
			timestamps.addLast(now);
		}

		cleanupOldKeys(now, windowMs);
		return true;
	}

	private void evictOldEntries(Deque<Long> timestamps, long now, long windowMs) {
		Long head = timestamps.peekFirst();
		while (head != null && now - head > windowMs) {
			timestamps.pollFirst();
			head = timestamps.peekFirst();
		}
	}

	private void cleanupOldKeys(long now, long windowMs) {
		long previousCleanup = lastCleanupEpochMs.get();
		if (now - previousCleanup < 30_000 || !lastCleanupEpochMs.compareAndSet(previousCleanup, now)) {
			return;
		}

		requestLog.entrySet().removeIf(entry -> {
			Deque<Long> timestamps = entry.getValue();
			synchronized (timestamps) {
				evictOldEntries(timestamps, now, windowMs);
				return timestamps.isEmpty();
			}
		});
	}

	private String resolveClientIp(HttpServletRequest request) {
		String forwarded = request.getHeader("X-Forwarded-For");
		if (forwarded != null && !forwarded.isBlank()) {
			return forwarded.split(",")[0].trim();
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}
}
