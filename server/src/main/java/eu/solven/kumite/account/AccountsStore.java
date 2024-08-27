package eu.solven.kumite.account;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.player.KumitePlayer;
import lombok.Value;

@Value
public class AccountsStore {
	// ContestPlayersRegistry contestPlayersRegistry;

	Map<UUID, KumiteAccount> accountIdToAccount = new ConcurrentHashMap<>();

	public KumitePlayer getAccountMainPlayer(UUID accountId) {
		KumiteAccount account = accountIdToAccount.get(accountId);

		if (account == null) {
			throw new IllegalArgumentException("No accountId=" + accountId);
		}

		return KumitePlayer.builder().playerId(account.getPlayerId()).build();
	}

	public KumiteAccount registerAccount(KumiteAccount kumiteAccount) {
		KumiteAccount alreadyIn = accountIdToAccount.putIfAbsent(kumiteAccount.getAccountId(), kumiteAccount);

		if (alreadyIn != null) {
			throw new IllegalArgumentException("Already account=" + kumiteAccount);
		}

		// contestPlayersRegistry.registerPlayer(null, null);

		return kumiteAccount;
	}

}
