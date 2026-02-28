package com.src.main.model;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "newsletter_subscriptions")
@Data
public class NewsletterSubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 1024)
    private String email;

    @Column(name = "email_hash", length = 64)
    private String emailHash;

    @Column(name = "subscribed_at", nullable = false)
    private OffsetDateTime subscribedAt;

    @Column(name = "welcome_email_sent", nullable = false)
    private boolean welcomeEmailSent;

    @Column(name = "sending_in_progress", nullable = false)
    private boolean sendingInProgress;

    @Column(name = "send_attempt_count", nullable = false)
    private int sendAttemptCount;

    @Column(name = "welcome_email_sent_at")
    private OffsetDateTime welcomeEmailSentAt;

    @Column(name = "last_send_error")
    private String lastSendError;
}
