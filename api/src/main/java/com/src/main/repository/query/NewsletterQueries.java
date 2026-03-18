package com.src.main.repository.query;

public final class NewsletterQueries {

	private NewsletterQueries() {
	}

	public static final String FIND_LEGACY_BY_EMAIL = """
			SELECT n
			  FROM NewsletterSubscriptionEntity n
			 WHERE n.email = :email
			 ORDER BY n.id ASC
			""";

	public static final String SET_EMAIL_HASH = """
			UPDATE NewsletterSubscriptionEntity n
			   SET n.emailHash = :emailHash
			 WHERE n.id = :id
			""";

	public static final String CLAIM_FOR_SENDING = """
			UPDATE NewsletterSubscriptionEntity n
			   SET n.sendingInProgress = true
			 WHERE n.id = :id
			   AND n.welcomeEmailSent = false
			   AND n.sendingInProgress = false
			""";

	public static final String MARK_SENT = """
			UPDATE NewsletterSubscriptionEntity n
			   SET n.welcomeEmailSent = true,
			       n.sendingInProgress = false,
			       n.sendAttemptCount = n.sendAttemptCount + 1,
			       n.welcomeEmailSentAt = :sentAt,
			       n.lastSendError = null
			 WHERE n.id = :id
			""";

	public static final String MARK_FAILED = """
			UPDATE NewsletterSubscriptionEntity n
			   SET n.sendingInProgress = false,
			       n.sendAttemptCount = n.sendAttemptCount + 1,
			       n.lastSendError = :error
			 WHERE n.id = :id
			""";
}
