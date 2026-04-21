package com.src.main.common.exception;

import java.util.Date;

public class ErrorDetails {
    private Date timestamp;
    private String message;
    private boolean isError;
    private int errorCode;


    public static class ErrorDetailsBuilder {
        private Date timestamp;
        private String message;
        private boolean isError;
        private int errorCode;

        ErrorDetailsBuilder() {
        }

        /**
         * @return {@code this}.
         */
        public ErrorDetails.ErrorDetailsBuilder timestamp(final Date timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ErrorDetails.ErrorDetailsBuilder message(final String message) {
            this.message = message;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ErrorDetails.ErrorDetailsBuilder isError(final boolean isError) {
            this.isError = isError;
            return this;
        }

        /**
         * @return {@code this}.
         */
        public ErrorDetails.ErrorDetailsBuilder errorCode(final int errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public ErrorDetails build() {
            return new ErrorDetails(this.timestamp, this.message, this.isError, this.errorCode);
        }

        @Override
        public String toString() {
            return "ErrorDetails.ErrorDetailsBuilder(timestamp=" + this.timestamp + ", message=" + this.message + ", isError=" + this.isError + ", errorCode=" + this.errorCode + ")";
        }
    }

    public static ErrorDetails.ErrorDetailsBuilder builder() {
        return new ErrorDetails.ErrorDetailsBuilder();
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public String getMessage() {
        return this.message;
    }

    public boolean isError() {
        return this.isError;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public void setTimestamp(final Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setError(final boolean isError) {
        this.isError = isError;
    }

    public void setErrorCode(final int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ErrorDetails)) return false;
        final ErrorDetails other = (ErrorDetails) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isError() != other.isError()) return false;
        if (this.getErrorCode() != other.getErrorCode()) return false;
        final Object this$timestamp = this.getTimestamp();
        final Object other$timestamp = other.getTimestamp();
        if (this$timestamp == null ? other$timestamp != null : !this$timestamp.equals(other$timestamp)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ErrorDetails;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isError() ? 79 : 97);
        result = result * PRIME + this.getErrorCode();
        final Object $timestamp = this.getTimestamp();
        result = result * PRIME + ($timestamp == null ? 43 : $timestamp.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "ErrorDetails(timestamp=" + this.getTimestamp() + ", message=" + this.getMessage() + ", isError=" + this.isError() + ", errorCode=" + this.getErrorCode() + ")";
    }

    public ErrorDetails(final Date timestamp, final String message, final boolean isError, final int errorCode) {
        this.timestamp = timestamp;
        this.message = message;
        this.isError = isError;
        this.errorCode = errorCode;
    }

    public ErrorDetails() {
    }
}
