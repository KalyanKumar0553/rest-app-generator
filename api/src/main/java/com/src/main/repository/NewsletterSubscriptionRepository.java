package com.src.main.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.src.main.model.NewsletterSubscriptionEntity;
import com.src.main.repository.query.NewsletterQueries;

public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscriptionEntity, Long> {

    @Query(NewsletterQueries.FIND_LEGACY_BY_EMAIL)
    List<NewsletterSubscriptionEntity> findLegacyByEmail(@Param("email") String email);

    boolean existsByEmailHash(String emailHash);

    @Modifying
    @Transactional
    @Query(NewsletterQueries.SET_EMAIL_HASH)
    int setEmailHash(@Param("id") Long id, @Param("emailHash") String emailHash);

    List<NewsletterSubscriptionEntity> findTop50ByWelcomeEmailSentFalseAndSendingInProgressFalseOrderBySubscribedAtAsc();

    @Modifying
    @Transactional
    @Query(NewsletterQueries.CLAIM_FOR_SENDING)
    int claimForSending(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(NewsletterQueries.MARK_SENT)
    int markSent(@Param("id") Long id, @Param("sentAt") OffsetDateTime sentAt);

    @Modifying
    @Transactional
    @Query(NewsletterQueries.MARK_FAILED)
    int markFailed(@Param("id") Long id, @Param("error") String error);
}
