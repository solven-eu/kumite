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
	Map<UUID, IGame> idToGame = new ConcurrentHashMap<>();

	public void registerGame(IGame game) {
		UUID gameId = game.getGameMetadata().getGameId();

		if (gameId == null) {
			throw new IllegalArgumentException("Missing gameId: " + game);
		}

		IGame alreadyIn = idToGame.putIfAbsent(gameId, game);
		if (alreadyIn != null) {
			throw new IllegalArgumentException("gameId already registered: " + game);
		}
	}

	public IGame getGame(UUID gameId) {
		IGame game = idToGame.get(gameId);
		if (game == null) {
			throw new IllegalArgumentException("No game registered for id=" + gameId);
		}
		return game;
	}

	public List<GameMetadata> searchGames(GameSearchParameters search) {
		Stream<GameMetadata> metaStream;

		if (search.getGameId().isPresent()) {
			UUID uuid = search.getGameId().get();
			metaStream = Optional.ofNullable(idToGame.get(uuid).getGameMetadata()).stream();
		} else {
			metaStream = idToGame.values().stream().map(c -> c.getGameMetadata());
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
