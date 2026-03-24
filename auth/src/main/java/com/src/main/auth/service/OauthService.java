package com.src.main.auth.service;

import java.net.URL;
import java.util.Map;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jose.proc.SimpleSecurityContext;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import com.src.main.auth.model.IdentifierType;
import com.src.main.auth.model.User;
import com.src.main.auth.model.UserProfile;
import com.src.main.auth.model.UserStatus;
import com.src.main.auth.repository.UserProfileRepository;
import com.src.main.auth.repository.UserRepository;
import com.src.main.auth.repository.UserRoleRepository;
import com.src.main.auth.util.CryptoUtils;

@Service
public class OauthService {
	public record OauthPrincipal(String email, String givenName, String familyName, String displayName, String avatarUrl) {}

	private final UserRepository userRepository;
	private final UserRoleRepository userRoleRepository;
	private final UserProfileRepository userProfileRepository;
	private final RoleCatalogService roleCatalogService;
	private final IdentifierLookupHashService identifierLookupHashService;
	private final String googleClientId;
	private final String appleClientId;

	public OauthService(
			UserRepository userRepository,
			UserRoleRepository userRoleRepository,
			UserProfileRepository userProfileRepository,
			RoleCatalogService roleCatalogService,
			IdentifierLookupHashService identifierLookupHashService,
			@Value("${oauth.google.client-id:}") String googleClientId,
			@Value("${oauth.apple.client-id:}") String appleClientId) {
		this.userRepository = userRepository;
		this.userRoleRepository = userRoleRepository;
		this.userProfileRepository = userProfileRepository;
		this.roleCatalogService = roleCatalogService;
		this.identifierLookupHashService = identifierLookupHashService;
		this.googleClientId = googleClientId;
		this.appleClientId = appleClientId;
	}

	public OauthPrincipal verifyGoogleIdToken(String idToken) {
		try {
			if (googleClientId == null || googleClientId.isBlank()) {
				throw new IllegalArgumentException("GOOGLE_CLIENT_ID not configured");
			}
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
					GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance())
					.setAudience(List.of(googleClientId))
					.build();
			GoogleIdToken token = verifier.verify(idToken);
			if (token == null) {
				throw new IllegalArgumentException("Invalid Google token");
			}
			String email = token.getPayload().getEmail();
			if (email == null || email.isBlank()) {
				throw new IllegalArgumentException("Email not present in Google token");
			}
			String givenName = stringClaim(token.getPayload().get("given_name"));
			String familyName = stringClaim(token.getPayload().get("family_name"));
			String displayName = stringClaim(token.getPayload().get("name"));
			String avatarUrl = stringClaim(token.getPayload().get("picture"));
			return new OauthPrincipal(email, givenName, familyName, displayName, avatarUrl);
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid Google token", ex);
		}
	}

	public OauthPrincipal extractGooglePrincipal(Map<String, Object> attributes) {
		return extractOauthPrincipal(attributes);
	}

	public OauthPrincipal extractOauthPrincipal(Map<String, Object> attributes) {
		String email = stringClaim(attributes.get("email"));
		if (email == null || email.isBlank()) {
			email = stringClaim(attributes.get("preferred_username"));
		}
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email not present in OAuth profile");
		}
		String givenName = stringClaim(attributes.get("given_name"));
		String familyName = stringClaim(attributes.get("family_name"));
		String displayName = stringClaim(attributes.get("name"));
		String avatarUrl = stringClaim(attributes.get("picture"));
		return new OauthPrincipal(email, givenName, familyName, displayName, avatarUrl);
	}

	public String verifyAppleIdentityToken(String identityToken) {
		try {
			if (appleClientId == null || appleClientId.isBlank()) {
				throw new IllegalArgumentException("APPLE_CLIENT_ID not configured");
			}
			ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
			JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(new URL("https://appleid.apple.com/auth/keys"));
			JWSKeySelector<SecurityContext> selector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
			processor.setJWSKeySelector(selector);
			JWTClaimsSet claims = processor.process(identityToken, new SimpleSecurityContext());
			if (!"https://appleid.apple.com".equals(claims.getIssuer())) {
				throw new IllegalArgumentException("Invalid Apple identity token");
			}
			if (claims.getAudience() == null || !claims.getAudience().contains(appleClientId)) {
				throw new IllegalArgumentException("Invalid Apple identity token");
			}
			String email = claims.getStringClaim("email");
			if (email == null || email.isBlank()) {
				throw new IllegalArgumentException("Email not present in Apple token");
			}
			return email;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Invalid Apple identity token", ex);
		}
	}

	public String upsertOauthUser(OauthPrincipal principal) {
		String identifier = principal.email().trim().toLowerCase();
		String identifierHash = identifierLookupHashService.hash(identifier);
		User existing = userRepository.findFirstByIdentifierHashOrIdentifier(identifierHash, identifier).orElse(null);
		if (existing != null) {
			if (existing.getIdentifierHash() == null || existing.getIdentifierHash().isBlank()) {
				existing.setIdentifierHash(identifierHash);
			}
			if (existing.getStatus() != UserStatus.ACTIVE) {
				existing.setStatus(UserStatus.ACTIVE);
			}
			userRepository.save(existing);
			upsertUserProfile(existing.getId(), principal);
			return existing.getId();
		}

		User user = new User();
		user.setIdentifier(identifier);
		user.setIdentifierHash(identifierHash);
		user.setIdentifierType(IdentifierType.EMAIL);
		user.setPasswordHash(CryptoUtils.hashPassword("oauth-" + CryptoUtils.uuid()));
		user.setStatus(UserStatus.ACTIVE);
		userRepository.save(user);

		UserRoleRepository userRoleRepo = userRoleRepository;
		com.src.main.auth.model.UserRole userRole = new com.src.main.auth.model.UserRole();
		userRole.setUserId(user.getId());
		userRole.setRoleName(roleCatalogService.getDefaultAuthRoleName());
		userRoleRepo.save(userRole);
		upsertUserProfile(user.getId(), principal);

		return user.getId();
	}

	public String upsertOauthUser(String email) {
		return upsertOauthUser(new OauthPrincipal(email, null, null, null, null));
	}

	private void upsertUserProfile(String userId, OauthPrincipal principal) {
		if (principal == null) {
			return;
		}

		UserProfile profile = userProfileRepository.findById(userId).orElseGet(() -> {
			UserProfile created = new UserProfile();
			created.setUserId(userId);
			return created;
		});

		profile.setFirstName(firstNonBlank(principal.givenName(), profile.getFirstName()));
		profile.setLastName(firstNonBlank(principal.familyName(), profile.getLastName()));
		profile.setAvatarUrl(firstNonBlank(principal.avatarUrl(), profile.getAvatarUrl()));

		if ((profile.getFirstName() == null || profile.getFirstName().isBlank())
				&& principal.displayName() != null
				&& !principal.displayName().isBlank()) {
			applyDisplayName(profile, principal.displayName());
		}

		userProfileRepository.save(profile);
	}

	private void applyDisplayName(UserProfile profile, String displayName) {
		String[] parts = displayName.trim().split("\\s+", 2);
		if (parts.length > 0 && (profile.getFirstName() == null || profile.getFirstName().isBlank())) {
			profile.setFirstName(parts[0]);
		}
		if (parts.length > 1 && (profile.getLastName() == null || profile.getLastName().isBlank())) {
			profile.setLastName(parts[1]);
		}
	}

	private String firstNonBlank(String preferred, String fallback) {
		if (preferred != null && !preferred.isBlank()) {
			return preferred;
		}
		return fallback;
	}

	private String stringClaim(Object value) {
		if (value == null) {
			return null;
		}
		String text = String.valueOf(value).trim();
		return text.isEmpty() ? null : text;
	}

}
