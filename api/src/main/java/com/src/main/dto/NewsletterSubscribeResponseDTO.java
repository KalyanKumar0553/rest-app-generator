package com.src.main.dto;

public class NewsletterSubscribeResponseDTO {
    private boolean subscribed;
    private String message;

    public boolean isSubscribed() {
        return this.subscribed;
    }

    public String getMessage() {
        return this.message;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NewsletterSubscribeResponseDTO)) return false;
        final NewsletterSubscribeResponseDTO other = (NewsletterSubscribeResponseDTO) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isSubscribed() != other.isSubscribed()) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NewsletterSubscribeResponseDTO;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isSubscribed() ? 79 : 97);
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "NewsletterSubscribeResponseDTO(subscribed=" + this.isSubscribed() + ", message=" + this.getMessage() + ")";
    }

    public NewsletterSubscribeResponseDTO(final boolean subscribed, final String message) {
        this.subscribed = subscribed;
        this.message = message;
    }
}
