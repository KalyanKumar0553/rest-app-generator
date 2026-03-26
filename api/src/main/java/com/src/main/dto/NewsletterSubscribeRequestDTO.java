package com.src.main.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class NewsletterSubscribeRequestDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    public NewsletterSubscribeRequestDTO() {
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof NewsletterSubscribeRequestDTO)) return false;
        final NewsletterSubscribeRequestDTO other = (NewsletterSubscribeRequestDTO) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof NewsletterSubscribeRequestDTO;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "NewsletterSubscribeRequestDTO(email=" + this.getEmail() + ")";
    }
}
