package com.src.main.config;

import java.time.Duration;

import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class InitializrGeneratorConfig {

	private final MavenCentralProperties props;

	public InitializrGeneratorConfig(MavenCentralProperties props) {
		this.props = props;
	}

	@Bean
	public WebClient mavenCentralWebClient() {
		return WebClient.builder().baseUrl(props.getBaseUrl())
				.exchangeStrategies(
						ExchangeStrategies.builder().codecs(c -> c.defaultCodecs().maxInMemorySize(256 * 1024)).build())
				.build();
	}
	
	@Bean
	CaffeineCacheManager cacheManager(CacheProperties cacheProperties) {
		CaffeineCacheManager mgr = new CaffeineCacheManager("depLookup");
        mgr.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(Duration.ofHours(12)));
        return mgr;
	}
}
