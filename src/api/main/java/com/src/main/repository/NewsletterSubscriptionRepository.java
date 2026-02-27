package com.src.main.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.NewsletterSubscriptionEntity;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscriptionEntity, Long> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO newsletter_subscriptions (email, subscribed_at, welcome_email_sent, sending_in_progress, send_attempt_count)
            VALUES (:email, :now, false, false, 0)
            ON CONFLICT (email)
            DO NOTHING
            """, nativeQuery = true)
    int insertIgnoreDuplicate(@Param("email") String email, @Param("now") OffsetDateTime now);

    List<NewsletterSubscriptionEntity> findTop50ByWelcomeEmailSentFalseAndSendingInProgressFalseOrderBySubscribedAtAsc();

    @Modifying
    @Transactional
    @Query("""
            UPDATE NewsletterSubscriptionEntity n
               SET n.sendingInProgress = true
             WHERE n.id = :id
               AND n.welcomeEmailSent = false
               AND n.sendingInProgress = false
            """)
    int claimForSending(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("""
            UPDATE NewsletterSubscriptionEntity n
               SET n.welcomeEmailSent = true,
                   n.sendingInProgress = false,
                   n.sendAttemptCount = n.sendAttemptCount + 1,
                   n.welcomeEmailSentAt = :sentAt,
                   n.lastSendError = null
             WHERE n.id = :id
            """)
    int markSent(@Param("id") Long id, @Param("sentAt") OffsetDateTime sentAt);

    @Modifying
    @Transactional
    @Query("""
            UPDATE NewsletterSubscriptionEntity n
               SET n.sendingInProgress = false,
                   n.sendAttemptCount = n.sendAttemptCount + 1,
                   n.lastSendError = :error
             WHERE n.id = :id
            """)
    int markFailed(@Param("id") Long id, @Param("error") String error);
}
