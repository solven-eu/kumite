package eu.solven.kumite.player;

import java.util.UUID;

import lombok.NonNull;

public interface IViewingAccountsRepository {

	void registerViewer(@NonNull UUID contestId, UUID accountId);

	boolean isViewing(UUID contestId, UUID accountId);

}
