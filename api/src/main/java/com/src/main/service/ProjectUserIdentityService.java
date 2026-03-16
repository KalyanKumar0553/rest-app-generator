package com.src.main.service;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.src.main.auth.model.User;
import com.src.main.auth.repository.UserRepository;
import com.src.main.auth.util.IdentifierUtils;

@Service
public class ProjectUserIdentityService {

	public record ResolvedProjectUser(String userId, Set<String> keys) {
	}

	private final UserRepository userRepository;

	public ProjectUserIdentityService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public String currentUserId(Principal principal) {
		if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
			throw new SecurityException("Authenticated user not found");
		}
		return resolve(principal.getName()).userId();
	}

	public ResolvedProjectUser resolve(String userKey) {
		if (userKey == null || userKey.isBlank()) {
			throw new SecurityException("Authenticated user not found");
		}

		String trimmed = userKey.trim();
		LinkedHashSet<String> keys = new LinkedHashSet<>();
		keys.add(trimmed);

		Optional<User> byId = userRepository.findById(trimmed);
		if (byId.isPresent()) {
			User user = byId.get();
			keys.add(user.getId());
			keys.add(user.getIdentifier());
			return new ResolvedProjectUser(user.getId(), keys);
		}

		Optional<String> normalized = normalizeIdentifier(trimmed);
		if (normalized.isPresent()) {
			keys.add(normalized.get());
			Optional<User> byIdentifier = userRepository.findByIdentifier(normalized.get());
			if (byIdentifier.isPresent()) {
				User user = byIdentifier.get();
				keys.add(user.getId());
				keys.add(user.getIdentifier());
				return new ResolvedProjectUser(user.getId(), keys);
			}
		}

		return new ResolvedProjectUser(trimmed, keys);
	}

	private Optional<String> normalizeIdentifier(String value) {
		try {
			return Optional.of(IdentifierUtils.normalizeIdentifier(value));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
