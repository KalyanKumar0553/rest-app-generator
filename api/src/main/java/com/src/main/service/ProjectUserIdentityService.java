package com.src.main.service;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.src.main.auth.model.User;
import com.src.main.auth.repository.UserRepository;
import com.src.main.auth.util.IdentifierUtils;

@Service
public class ProjectUserIdentityService {

	private static final Logger log = LoggerFactory.getLogger(ProjectUserIdentityService.class);

	public record ResolvedProjectUser(String userId, Set<String> keys) {
	}

	private final UserRepository userRepository;
	private final DataEncryptionService dataEncryptionService;

	public ProjectUserIdentityService(UserRepository userRepository, DataEncryptionService dataEncryptionService) {
		this.userRepository = userRepository;
		this.dataEncryptionService = dataEncryptionService;
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
			ResolvedProjectUser resolved = new ResolvedProjectUser(user.getId(), keys);
			log.debug("Resolved project user by id: inputKey='{}', resolvedUserId='{}', resolvedKeys={}",
					trimmed, resolved.userId(), resolved.keys());
			return resolved;
		}

		Optional<String> normalized = normalizeIdentifier(trimmed);
		if (normalized.isPresent()) {
			keys.add(normalized.get());
			String identifierHash = dataEncryptionService.hashForLookup(normalized.get());
			Optional<User> byIdentifier = userRepository.findFirstByIdentifierHashOrIdentifier(identifierHash, normalized.get());
			if (byIdentifier.isPresent()) {
				User user = byIdentifier.get();
				if (user.getIdentifierHash() == null || user.getIdentifierHash().isBlank()) {
					user.setIdentifierHash(identifierHash);
					userRepository.save(user);
				}
				keys.add(user.getId());
				keys.add(user.getIdentifier());
				ResolvedProjectUser resolved = new ResolvedProjectUser(user.getId(), keys);
				log.debug("Resolved project user by identifier: inputKey='{}', normalizedIdentifier='{}', resolvedUserId='{}', resolvedKeys={}",
						trimmed, normalized.get(), resolved.userId(), resolved.keys());
				return resolved;
			}
		}

		ResolvedProjectUser resolved = new ResolvedProjectUser(trimmed, keys);
		log.debug("Resolved project user by fallback: inputKey='{}', resolvedUserId='{}', resolvedKeys={}",
				trimmed, resolved.userId(), resolved.keys());
		return resolved;
	}

	private Optional<String> normalizeIdentifier(String value) {
		try {
			return Optional.of(IdentifierUtils.normalizeIdentifier(value));
		} catch (IllegalArgumentException ex) {
			return Optional.empty();
		}
	}
}
