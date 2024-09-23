package eu.solven.kumite.oauth2.authorizationserver;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ActiveRefreshTokens {
	final Map<UUID, Set<UUID>> accountIdToJti = new ConcurrentHashMap<>();

	public void touchRefreshToken(UUID accountId, UUID jti) {
		accountIdToJti.computeIfAbsent(accountId, k -> new ConcurrentSkipListSet<>()).add(jti);
	}

	public Set<UUID> getActiveTokens(UUID accountId) {
		return accountIdToJti.getOrDefault(accountId, Set.of());
	}
}
