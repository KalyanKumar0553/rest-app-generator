package com.src.main.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.communication.service.MsgService;
import com.src.main.model.NewsletterSubscriptionEntity;
import com.src.main.repository.NewsletterSubscriptionRepository;
import com.src.main.util.AppConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsletterSubscriptionServiceImpl implements NewsletterSubscriptionService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(AppConstants.emailRegex);

    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final MsgService msgService;
    private final DataEncryptionService dataEncryptionService;

    @Value("${app.newsletter.email.from:no-reply@bootrid.io}")
    private String emailFrom;

    @Value("${app.newsletter.email.subject:Welcome to BootRid updates}")
    private String emailSubject;

    @Override
    @Transactional
    public boolean subscribe(String email) {
        String normalizedEmail = normalizeEmail(email);
        String emailHash = dataEncryptionService.hashForLookup(normalizedEmail);
        String encryptedEmail = dataEncryptionService.encrypt(normalizedEmail);
        OffsetDateTime now = OffsetDateTime.now();
        int inserted = newsletterSubscriptionRepository.insertIgnoreDuplicate(encryptedEmail, emailHash, now);
        if (inserted == 0) {
            // Backward compatibility for legacy plain-text rows created before encryption rollout.
            var legacyRows = newsletterSubscriptionRepository.findLegacyByEmail(normalizedEmail);
            for (NewsletterSubscriptionEntity row : legacyRows) {
                if (row.getId() != null && row.getEmailHash() == null && !newsletterSubscriptionRepository.existsByEmailHash(emailHash)) {
                    newsletterSubscriptionRepository.setEmailHash(row.getId(), emailHash);
                }
            }
        }
        return inserted > 0;
    }

    @Override
    @Scheduled(fixedDelayString = "${app.newsletter.scheduler.fixed-delay-ms:180000}")
    public void processPendingWelcomeEmails() {
        List<NewsletterSubscriptionEntity> pending = newsletterSubscriptionRepository
                .findTop50ByWelcomeEmailSentFalseAndSendingInProgressFalseOrderBySubscribedAtAsc();

        for (NewsletterSubscriptionEntity subscription : pending) {
            Long id = subscription.getId();
            if (id == null) {
                continue;
            }

            int claimed = claimForSending(id);
            if (claimed == 0) {
                continue;
            }

            try {
                String decryptedEmail = dataEncryptionService.decrypt(subscription.getEmail());
                if (decryptedEmail == null || decryptedEmail.isBlank()) {
                    throw new IllegalArgumentException("Decrypted email is empty");
                }
                String htmlBody = buildWelcomeHtml(decryptedEmail);
                msgService.sendEmail(emailFrom, decryptedEmail, emailSubject, htmlBody);
                markSent(id);
            } catch (Exception ex) {
                String error = ex.getMessage() == null ? "Email send failed" : ex.getMessage();
                markFailed(id, abbreviate(error, 800));
                log.warn("Failed to send newsletter welcome email for subscription id={}", id, ex);
            }
        }
    }

    @Transactional
    protected int claimForSending(Long id) {
        return newsletterSubscriptionRepository.claimForSending(id);
    }

    @Transactional
    protected void markSent(Long id) {
        newsletterSubscriptionRepository.markSent(id, OffsetDateTime.now());
    }

    @Transactional
    protected void markFailed(Long id, String error) {
        newsletterSubscriptionRepository.markFailed(id, error);
    }

    private String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();
        if (normalized.isBlank() || !EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Please provide a valid email");
        }
        return normalized;
    }

    private String buildWelcomeHtml(String recipient) {
        String safeRecipient = recipient == null ? "" : recipient;
        return """
                <div style=\"font-family:Arial,sans-serif;line-height:1.6;color:#333;\">
                  <h2 style=\"margin:0 0 12px;\">Welcome to BootRid updates</h2>
                  <p style=\"margin:0 0 10px;\">Hi %s,</p>
                  <p style=\"margin:0 0 10px;\">Thanks for subscribing to BootRid product updates.</p>
                  <p style=\"margin:0;\">We will share important releases and improvements with you.</p>
                </div>
                """.formatted(safeRecipient);
    }

    private String abbreviate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
