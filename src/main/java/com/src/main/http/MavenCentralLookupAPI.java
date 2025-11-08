package com.src.main.http;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import com.src.main.config.MavenCentralProperties;
import com.src.main.dto.MavenDependency;
import com.src.main.utils.AppConstants;

import reactor.netty.http.client.HttpClient;

@Component
public class MavenCentralLookupAPI implements RemoteDependencyLookup {

	private static final Logger log = LoggerFactory.getLogger(MavenCentralLookupAPI.class);
	private final WebClient client;
	private final MavenCentralProperties props;

	public MavenCentralLookupAPI(WebClient mavenCentralWebClient, MavenCentralProperties props) {
		this.props = props;
		HttpClient httpClient = HttpClient.create().responseTimeout(props.getReadTimeout()).compress(true);
		this.client = mavenCentralWebClient.mutate().clientConnector(new ReactorClientHttpConnector(httpClient)).build();
	}

	@Override
	@Cacheable(cacheNames = "depLookup", key = "#keyword", unless = "#result == null")
	public Optional<MavenDependency> findByKeyword(String keyword) {
		if (!StringUtils.hasText(keyword))
			return Optional.empty();
		String query = AppConstants.MAVEN_URL.formatted(keyword.trim());
		try {
			var root = client.get().uri(query).retrieve().bodyToMono(org.json.JSONObject.class)
					.block(props.getReadTimeout());
			if (root == null)
				return Optional.empty();

			var resp = root.getJSONObject("response");
			if (resp.getInt("numFound") == 0)
				return Optional.empty();

			var doc = resp.getJSONArray("docs").getJSONObject(0);
			String g = doc.getString("g");
			String a = doc.getString("a");
			return Optional.of(new MavenDependency(g, a, null, false));
		} catch (Exception ex) {
			log.warn("Maven Central lookup failed for '{}': {}", keyword, ex.toString());
			return Optional.empty();
		}
	}
}
