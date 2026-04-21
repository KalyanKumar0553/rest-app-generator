package com.src.main.common.exception;

public class AbstractRuntimeException extends RuntimeException {
	private String message;
	private int errorCode;

	public AbstractRuntimeException(int errorCode, String message) {
		super(message);
		this.message = message;
		this.errorCode = errorCode;
	}

	public String getMessage() {
		return this.message;
	}

	public int getErrorCode() {
		return this.errorCode;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setErrorCode(final int errorCode) {
		this.errorCode = errorCode;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof AbstractRuntimeException)) return false;
		final AbstractRuntimeException other = (AbstractRuntimeException) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.getErrorCode() != other.getErrorCode()) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof AbstractRuntimeException;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getErrorCode();
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "AbstractRuntimeException(message=" + this.getMessage() + ", errorCode=" + this.getErrorCode() + ")";
	}
}
