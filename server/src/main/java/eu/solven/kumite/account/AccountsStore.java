package eu.solven.kumite.account;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.player.AccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AccountsStore {
	final AccountPlayersRegistry accountPlayersRegistry;

	Map<UUID, KumiteAccount> accountIdToAccount = new ConcurrentHashMap<>();

	public KumitePlayer getAccountMainPlayer(UUID accountId) {
		KumiteAccount account = accountIdToAccount.get(accountId);

		if (account == null) {
			throw new IllegalArgumentException("No accountId=" + accountId);
		}

		return KumitePlayer.builder().playerId(account.getPlayerId()).build();
	}

	public KumiteAccount registerAccount(KumiteAccount kumiteAccount) {
		UUID accountId = kumiteAccount.getAccountId();
		KumiteAccount alreadyIn = accountIdToAccount.putIfAbsent(accountId, kumiteAccount);

		if (alreadyIn != null) {
			throw new IllegalArgumentException("Already account=" + kumiteAccount);
		}

		accountPlayersRegistry.registerPlayer(accountId, getAccountMainPlayer(accountId));

		return kumiteAccount;
	}

}
