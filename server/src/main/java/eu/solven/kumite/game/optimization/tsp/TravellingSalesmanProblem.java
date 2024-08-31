package eu.solven.kumite.game.optimization.tsp;

import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.random.RandomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.TSPBoard.TSPBoardBuilder;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.LeaderBoard;
import eu.solven.kumite.leaderboard.PlayerDoubleScore;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import lombok.Value;

@Value
public class TravellingSalesmanProblem implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("02df90d8-e7aa-4b07-8a70-6f8cb358e9bb"))
			.title("Travelling Salesman Problem")
			.maxPlayers(Integer.MAX_VALUE)
			.shortDescription(
					"Given a list of cities and the distances between each pair of cities, what is the shortest possible route that visits each city exactly once and returns to the origin city?")
			.reference(URI.create("https://en.wikipedia.org/wiki/Travelling_salesman_problem"))
			.build();

	Function<RandomGenerator, TSPBoard> examplesSupplier = random -> {
		TSPBoardBuilder pBuilder = TSPBoard.builder();

		// TODO Introduce a difficulty parameters, typically associated to the time to find an optimal solution, based
		// on previous contests
		for (int i = 0; i < 128; i++) {
			pBuilder.city(TSPCity.builder().name("city_" + i).x(random.nextDouble()).y(random.nextDouble()).build());

		}

		return pBuilder.build();
	};

	ToDoubleBiFunction<TSPBoard, TSPSolution> solutionToScore = (p, s) -> {
		Map<String, TSPCity> nameToCity = new TreeMap<>();
		p.getCities().forEach(c -> nameToCity.put(c.getName(), c));

		Set<String> visitedCities = new HashSet<>();
		s.getCities().forEach(c -> visitedCities.add(c));

		if (s.getCities().size() <= 1) {
			// edge-case none or single city: the travel is instantaneous (supposing we checked this is a legit
			// solution)
			return 0D;
		}

		String previousCity = null;

		double cost = 0D;

		for (String city : s.getCities()) {
			if (previousCity != null) {
				// We do not get into this block only for the first city
				cost += distance(nameToCity, previousCity, city);
			}

			visitedCities.add(city);
			previousCity = city;
		}

		// Add the cost to go back to the initial city
		cost += distance(nameToCity, previousCity, s.getCities().get(0));

		return cost;
	};

	private double distance(Map<String, TSPCity> nameToCity, String previousCity, String city) {
		TSPCity from = nameToCity.get(previousCity);
		TSPCity to = nameToCity.get(city);

		double xSquared = Math.pow(from.getX() - to.getX(), 2);
		double ySquared = Math.pow(from.getY() - to.getY(), 2);

		return Math.sqrt(xSquared + ySquared);
	}

	@Override
	public boolean isValidMove(IKumiteMove move) {
		return true;
	}

	@Override
	public TSPBoard generateInitialBoard(RandomGenerator random) {
		return examplesSupplier.apply(random);
	}

	@Override
	public boolean canAcceptPlayer(ContestMetadata contest, KumitePlayer player) {
		return true;
	}

	@Override
	public TSPSolution parseRawMove(Map<String, ?> rawMove) {
		return new ObjectMapper().convertValue(rawMove, TSPSolution.class);
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return new ObjectMapper().convertValue(rawBoard, TSPBoard.class);
	}

	@Override
	public LeaderBoard makeLeaderboard(IKumiteBoard board) {
		Map<UUID, IPlayerScore> playerToScore = new TreeMap<>();

		TSPBoard tspBoard = (TSPBoard) board;
		tspBoard.getPlayerToLatestSolution().forEach((playerId, solution) -> {
			double score = solutionToScore.applyAsDouble(tspBoard, solution);
			playerToScore.put(playerId, PlayerDoubleScore.builder().playerId(playerId).score(score).build());
		});

		return LeaderBoard.builder().playerIdToPlayerScore(playerToScore).build();
	}
}
