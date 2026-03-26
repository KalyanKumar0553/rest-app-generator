package com.src.main.common.dto;

public class JSONResponseDTO<T> {
	T data;
	private boolean success;
	private String message;
	boolean isError;


	public static class JSONResponseDTOBuilder<T> {
		private T data;
		private boolean success;
		private String message;
		private boolean isError;

		JSONResponseDTOBuilder() {
		}

		/**
		 * @return {@code this}.
		 */
		public JSONResponseDTO.JSONResponseDTOBuilder<T> data(final T data) {
			this.data = data;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public JSONResponseDTO.JSONResponseDTOBuilder<T> success(final boolean success) {
			this.success = success;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public JSONResponseDTO.JSONResponseDTOBuilder<T> message(final String message) {
			this.message = message;
			return this;
		}

		/**
		 * @return {@code this}.
		 */
		public JSONResponseDTO.JSONResponseDTOBuilder<T> isError(final boolean isError) {
			this.isError = isError;
			return this;
		}

		public JSONResponseDTO<T> build() {
			return new JSONResponseDTO<T>(this.data, this.success, this.message, this.isError);
		}

		@Override
		public String toString() {
			return "JSONResponseDTO.JSONResponseDTOBuilder(data=" + this.data + ", success=" + this.success + ", message=" + this.message + ", isError=" + this.isError + ")";
		}
	}

	public static <T> JSONResponseDTO.JSONResponseDTOBuilder<T> builder() {
		return new JSONResponseDTO.JSONResponseDTOBuilder<T>();
	}

	public T getData() {
		return this.data;
	}

	public boolean isSuccess() {
		return this.success;
	}

	public String getMessage() {
		return this.message;
	}

	public boolean isError() {
		return this.isError;
	}

	public void setData(final T data) {
		this.data = data;
	}

	public void setSuccess(final boolean success) {
		this.success = success;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	public void setError(final boolean isError) {
		this.isError = isError;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof JSONResponseDTO)) return false;
		final JSONResponseDTO<?> other = (JSONResponseDTO<?>) o;
		if (!other.canEqual((Object) this)) return false;
		if (this.isSuccess() != other.isSuccess()) return false;
		if (this.isError() != other.isError()) return false;
		final Object this$data = this.getData();
		final Object other$data = other.getData();
		if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
		final Object this$message = this.getMessage();
		final Object other$message = other.getMessage();
		if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof JSONResponseDTO;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + (this.isSuccess() ? 79 : 97);
		result = result * PRIME + (this.isError() ? 79 : 97);
		final Object $data = this.getData();
		result = result * PRIME + ($data == null ? 43 : $data.hashCode());
		final Object $message = this.getMessage();
		result = result * PRIME + ($message == null ? 43 : $message.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "JSONResponseDTO(data=" + this.getData() + ", success=" + this.isSuccess() + ", message=" + this.getMessage() + ", isError=" + this.isError() + ")";
	}

	public JSONResponseDTO(final T data, final boolean success, final String message, final boolean isError) {
		this.data = data;
		this.success = success;
		this.message = message;
		this.isError = isError;
	}

	public JSONResponseDTO() {
	}
}
