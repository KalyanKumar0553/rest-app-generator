package com.src.main.exception;

import org.springframework.http.HttpStatus;

public class GenericException extends RuntimeException {
	private HttpStatus status;
	private String errorMsg;

	public GenericException(HttpStatus status, String errorMsg) {
		super(errorMsg);
		this.status = status;
		this.errorMsg = errorMsg;
	}

	public HttpStatus getStatus() {
		return this.status;
	}

	public String getErrorMsg() {
		return this.errorMsg;
	}

	public void setStatus(final HttpStatus status) {
		this.status = status;
	}

	public void setErrorMsg(final String errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof GenericException)) return false;
		final GenericException other = (GenericException) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$status = this.getStatus();
		final Object other$status = other.getStatus();
		if (this$status == null ? other$status != null : !this$status.equals(other$status)) return false;
		final Object this$errorMsg = this.getErrorMsg();
		final Object other$errorMsg = other.getErrorMsg();
		if (this$errorMsg == null ? other$errorMsg != null : !this$errorMsg.equals(other$errorMsg)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof GenericException;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $status = this.getStatus();
		result = result * PRIME + ($status == null ? 43 : $status.hashCode());
		final Object $errorMsg = this.getErrorMsg();
		result = result * PRIME + ($errorMsg == null ? 43 : $errorMsg.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "GenericException(status=" + this.getStatus() + ", errorMsg=" + this.getErrorMsg() + ")";
	}

	public GenericException() {
	}
}
