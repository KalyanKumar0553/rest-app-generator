package com.src.main.auth.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class APIHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        boolean isHealthy = checkCustomService();
        if (isHealthy) {
            return Health.up().withDetail("API", "Running").build();
        } else {
            return Health.down().withDetail("API", "Down").build();
        }
    }

    private boolean checkCustomService() {
        // Your custom logic to check health (e.g., database status, external API)
        return true;
    }
}
