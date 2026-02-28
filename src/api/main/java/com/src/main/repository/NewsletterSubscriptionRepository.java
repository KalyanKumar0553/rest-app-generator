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
            INSERT INTO newsletter_subscriptions (email, email_hash, subscribed_at, welcome_email_sent, sending_in_progress, send_attempt_count)
            VALUES (:email, :emailHash, :now, false, false, 0)
            ON CONFLICT (email_hash)
            DO NOTHING
            """, nativeQuery = true)
    int insertIgnoreDuplicate(@Param("email") String email, @Param("emailHash") String emailHash, @Param("now") OffsetDateTime now);

    @Query("""
            SELECT n
              FROM NewsletterSubscriptionEntity n
             WHERE n.email = :email
             ORDER BY n.id ASC
            """)
    List<NewsletterSubscriptionEntity> findLegacyByEmail(@Param("email") String email);

    boolean existsByEmailHash(String emailHash);

    @Modifying
    @Transactional
    @Query("""
            UPDATE NewsletterSubscriptionEntity n
               SET n.emailHash = :emailHash
             WHERE n.id = :id
            """)
    int setEmailHash(@Param("id") Long id, @Param("emailHash") String emailHash);

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
