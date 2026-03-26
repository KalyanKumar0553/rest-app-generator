package com.src.main.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "generator.maven-central")
@Validated
public class MavenCentralProperties {
	@NotBlank
	private String baseUrl;
	@NotNull
	private Duration connectTimeout = Duration.ofSeconds(3);
	@NotNull
	private Duration readTimeout = Duration.ofSeconds(4);

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public Duration getConnectTimeout() {
		return this.connectTimeout;
	}

	public Duration getReadTimeout() {
		return this.readTimeout;
	}

	public void setBaseUrl(final String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setConnectTimeout(final Duration connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(final Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof MavenCentralProperties)) return false;
		final MavenCentralProperties other = (MavenCentralProperties) o;
		if (!other.canEqual((Object) this)) return false;
		final Object this$baseUrl = this.getBaseUrl();
		final Object other$baseUrl = other.getBaseUrl();
		if (this$baseUrl == null ? other$baseUrl != null : !this$baseUrl.equals(other$baseUrl)) return false;
		final Object this$connectTimeout = this.getConnectTimeout();
		final Object other$connectTimeout = other.getConnectTimeout();
		if (this$connectTimeout == null ? other$connectTimeout != null : !this$connectTimeout.equals(other$connectTimeout)) return false;
		final Object this$readTimeout = this.getReadTimeout();
		final Object other$readTimeout = other.getReadTimeout();
		if (this$readTimeout == null ? other$readTimeout != null : !this$readTimeout.equals(other$readTimeout)) return false;
		return true;
	}

	protected boolean canEqual(final Object other) {
		return other instanceof MavenCentralProperties;
	}

	@Override
	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		final Object $baseUrl = this.getBaseUrl();
		result = result * PRIME + ($baseUrl == null ? 43 : $baseUrl.hashCode());
		final Object $connectTimeout = this.getConnectTimeout();
		result = result * PRIME + ($connectTimeout == null ? 43 : $connectTimeout.hashCode());
		final Object $readTimeout = this.getReadTimeout();
		result = result * PRIME + ($readTimeout == null ? 43 : $readTimeout.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return "MavenCentralProperties(baseUrl=" + this.getBaseUrl() + ", connectTimeout=" + this.getConnectTimeout() + ", readTimeout=" + this.getReadTimeout() + ")";
	}

	public MavenCentralProperties(final String baseUrl, final Duration connectTimeout, final Duration readTimeout) {
		this.baseUrl = baseUrl;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	public MavenCentralProperties() {
	}
}
