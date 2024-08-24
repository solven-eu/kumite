package eu.solven.kumite.game;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Value;

@Value
public class GamesStore {
	Map<UUID, IGame> uuidToGame = new ConcurrentHashMap<>();

	public void registerContest(IGame c) {
		uuidToGame.put(c.getGameMetadata().getGameId(), c);
	}

	public IGame getGame(UUID gameUuid) {
		IGame contest = uuidToGame.get(gameUuid);
		if (contest == null) {
			throw new IllegalArgumentException("No game registered for uuid=" + gameUuid);
		}
		return contest;
	}

	public List<GameMetadata> searchGames(GameSearchParameters search) {
		Stream<GameMetadata> metaStream;

		if (search.getGameUuid().isPresent()) {
			UUID uuid = search.getGameUuid().get();
			metaStream = Optional.ofNullable(uuidToGame.get(uuid).getGameMetadata()).stream();
		} else {
			metaStream = uuidToGame.values().stream().map(c -> c.getGameMetadata());
		}

		if (search.getMinPlayers().isPresent()) {
			// User wants N minPlayers: he accepts N and N + 1 players
			metaStream = metaStream.filter(c -> c.getMinPlayers() >= search.getMinPlayers().getAsInt());
		}

		if (search.getMaxPlayers().isPresent()) {
			// User wants N maxPlayers: he accepts N and N - 1 players
			metaStream = metaStream.filter(c -> c.getMaxPlayers() <= search.getMaxPlayers().getAsInt());
		}

		if (search.getTitlePattern().isPresent()) {
			Predicate<String> titlePredicate = Pattern.compile(search.getTitlePattern().get()).asMatchPredicate();
			metaStream = metaStream.filter(c -> titlePredicate.test(c.getTitle()));
		}

		return metaStream.collect(Collectors.toList());
	}
}
