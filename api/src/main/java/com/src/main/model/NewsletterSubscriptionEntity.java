package com.src.main.model;

import java.time.OffsetDateTime;
import com.src.main.config.AppDbTables;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = AppDbTables.NEWSLETTER_SUBSCRIPTIONS)
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

    public NewsletterSubscriptionEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public String getEmailHash() {
        return this.emailHash;
    }

    public OffsetDateTime getSubscribedAt() {
        return this.subscribedAt;
    }

    public boolean isWelcomeEmailSent() {
        return this.welcomeEmailSent;
    }

    public boolean isSendingInProgress() {
        return this.sendingInProgress;
    }

    public int getSendAttemptCount() {
        return this.sendAttemptCount;
    }

    public OffsetDateTime getWelcomeEmailSentAt() {
        return this.welcomeEmailSentAt;
    }

    public String getLastSendError() {
        return this.lastSendError;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public void setEmailHash(final String emailHash) {
        this.emailHash = emailHash;
    }

    public void setSubscribedAt(final OffsetDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public void setWelcomeEmailSent(final boolean welcomeEmailSent) {
        this.welcomeEmailSent = welcomeEmailSent;
    }

    public void setSendingInProgress(final boolean sendingInProgress) {
        this.sendingInProgress = sendingInProgress;
    }

    public void setSendAttemptCount(final int sendAttemptCount) {
        this.sendAttemptCount = sendAttemptCount;
    }

    public void setWelcomeEmailSentAt(final OffsetDateTime welcomeEmailSentAt) {
        this.welcomeEmailSentAt = welcomeEmailSentAt;
    }

    public void setLastSendError(final String lastSendError) {
        this.lastSendError = lastSendError;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NewsletterSubscriptionEntity)) return false;
        final NewsletterSubscriptionEntity other = (NewsletterSubscriptionEntity) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isWelcomeEmailSent() != other.isWelcomeEmailSent()) return false;
        if (this.isSendingInProgress() != other.isSendingInProgress()) return false;
        if (this.getSendAttemptCount() != other.getSendAttemptCount()) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$emailHash = this.getEmailHash();
        final Object other$emailHash = other.getEmailHash();
        if (this$emailHash == null ? other$emailHash != null : !this$emailHash.equals(other$emailHash)) return false;
        final Object this$subscribedAt = this.getSubscribedAt();
        final Object other$subscribedAt = other.getSubscribedAt();
        if (this$subscribedAt == null ? other$subscribedAt != null : !this$subscribedAt.equals(other$subscribedAt)) return false;
        final Object this$welcomeEmailSentAt = this.getWelcomeEmailSentAt();
        final Object other$welcomeEmailSentAt = other.getWelcomeEmailSentAt();
        if (this$welcomeEmailSentAt == null ? other$welcomeEmailSentAt != null : !this$welcomeEmailSentAt.equals(other$welcomeEmailSentAt)) return false;
        final Object this$lastSendError = this.getLastSendError();
        final Object other$lastSendError = other.getLastSendError();
        if (this$lastSendError == null ? other$lastSendError != null : !this$lastSendError.equals(other$lastSendError)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NewsletterSubscriptionEntity;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isWelcomeEmailSent() ? 79 : 97);
        result = result * PRIME + (this.isSendingInProgress() ? 79 : 97);
        result = result * PRIME + this.getSendAttemptCount();
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $emailHash = this.getEmailHash();
        result = result * PRIME + ($emailHash == null ? 43 : $emailHash.hashCode());
        final Object $subscribedAt = this.getSubscribedAt();
        result = result * PRIME + ($subscribedAt == null ? 43 : $subscribedAt.hashCode());
        final Object $welcomeEmailSentAt = this.getWelcomeEmailSentAt();
        result = result * PRIME + ($welcomeEmailSentAt == null ? 43 : $welcomeEmailSentAt.hashCode());
        final Object $lastSendError = this.getLastSendError();
        result = result * PRIME + ($lastSendError == null ? 43 : $lastSendError.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "NewsletterSubscriptionEntity(id=" + this.getId() + ", email=" + this.getEmail() + ", emailHash=" + this.getEmailHash() + ", subscribedAt=" + this.getSubscribedAt() + ", welcomeEmailSent=" + this.isWelcomeEmailSent() + ", sendingInProgress=" + this.isSendingInProgress() + ", sendAttemptCount=" + this.getSendAttemptCount() + ", welcomeEmailSentAt=" + this.getWelcomeEmailSentAt() + ", lastSendError=" + this.getLastSendError() + ")";
    }
}
