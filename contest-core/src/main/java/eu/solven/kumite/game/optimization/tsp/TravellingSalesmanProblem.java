package eu.solven.kumite.game.optimization.tsp;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.leaderboard.PlayerDoubleScore;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.Value;

@Value
public class TravellingSalesmanProblem implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("02df90d8-e7aa-4b07-8a70-6f8cb358e9bb"))
			.title("Travelling Salesman Problem")
			.tag(IGameMetadataConstants.TAG_OPTIMIZATION)
			.tag(IGameMetadataConstants.TAG_PERFECT_INFORMATION)
			.maxPlayers(1024)
			.shortDescription(
					"Given a list of cities and the distances between each pair of cities, what is the shortest possible route that visits each city exactly once and returns to the origin city?")
			.reference(URI.create("https://en.wikipedia.org/wiki/Travelling_salesman_problem"))
			.build();

	Function<RandomGenerator, TSPBoard> boardGenerator = random -> {
		TSPProblem.TSPProblemBuilder pBuilder = TSPProblem.builder();

		// TODO Introduce a difficulty parameters, typically associated to the time to find an optimal solution, based
		// on previous contests
		for (int i = 0; i < 128; i++) {
			pBuilder.city(TSPCity.builder().name("city_" + i).x(random.nextDouble()).y(random.nextDouble()).build());
		}

		return TSPBoard.builder().problem(pBuilder.build()).build();
	};

	ToDoubleBiFunction<TSPBoard, TSPSolution> solutionToScore = (p, s) -> {
		if (!p.getProblem().getCities().isEmpty() && s.getCities().isEmpty()) {
			// The problem is not empty while the solution is empty: this is the initial solution on player registration
			return Double.MAX_VALUE;
		}

		Map<String, TSPCity> nameToCity = new TreeMap<>();
		p.getProblem().getCities().forEach(c -> nameToCity.put(c.getName(), c));

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

		return distance(from, to);
	}

	private double distance(TSPCity from, TSPCity to) {
		double xSquared = Math.pow(from.getX() - to.getX(), 2);
		double ySquared = Math.pow(from.getY() - to.getY(), 2);

		return Math.sqrt(xSquared + ySquared);
	}

	@Override
	public TSPBoard generateInitialBoard(RandomGenerator random) {
		return boardGenerator.apply(random);
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return new ObjectMapper().convertValue(rawBoard, TSPBoard.class);
	}

	@Override
	public List<String> invalidMoveReasons(IKumiteBoardView rawBoardView, PlayerMoveRaw playerMove) {
		TSPProblem p = (TSPProblem) rawBoardView;

		TSPSolution s = (TSPSolution) playerMove.getMove();

		List<String> invalidReasons = new ArrayList<>();

		if (p.getCities().size() != s.getCities().size()) {
			invalidReasons.add("Inconsistent number of cities");
		}

		Map<String, TSPCity> nameToCity = new HashMap<>();
		p.getCities().forEach(c -> nameToCity.put(c.getName(), c));

		Set<String> visitedCities = new HashSet<>();
		s.getCities().forEach(c -> visitedCities.add(c));

		if (!nameToCity.keySet().equals(visitedCities)) {
			invalidReasons.add("Inconsistent set of cities");
		}

		return invalidReasons;
	}

	@Override
	public TSPSolution parseRawMove(Map<String, ?> rawMove) {
		return new ObjectMapper().convertValue(rawMove, TSPSolution.class);
	}

	@Override
	public Leaderboard makeLeaderboard(IKumiteBoard board) {
		Map<UUID, IPlayerScore> playerToScore = new TreeMap<>();

		TSPBoard tspBoard = (TSPBoard) board;
		tspBoard.getPlayerToLatestSolution().forEach((playerId, solution) -> {
			double score = solutionToScore.applyAsDouble(tspBoard, solution);
			playerToScore.put(playerId, PlayerDoubleScore.builder().playerId(playerId).score(score).build());
		});

		return Leaderboard.builder().playerIdToPlayerScore(playerToScore).build();
	}

	@Override
	public Map<String, IKumiteMove> exampleMoves(RandomGenerator randomGenerator,
			IKumiteBoardView boardView,
			UUID playerId) {
		TSPProblem problem = (TSPProblem) boardView;

		TSPSolution lexicographicalMove;
		{
			List<String> cityNames = problem.getCities().stream().map(c -> c.getName()).collect(Collectors.toList());
			lexicographicalMove = TSPSolution.builder().cities(cityNames).build();
		}

		TSPSolution randomMove;
		{
			List<String> cityNames = problem.getCities().stream().map(c -> c.getName()).collect(Collectors.toList());
			Collections.shuffle(cityNames, randomGenerator);
			randomMove = TSPSolution.builder().cities(cityNames).build();
		}

		TSPSolution greedyMove;
		{

			List<TSPCity> orderedCities = new ArrayList<>();

			Map<String, TSPCity> nameToCity = new HashMap<>();
			problem.getCities().forEach(city -> nameToCity.put(city.getName(), city));

			while (!nameToCity.isEmpty()) {
				if (orderedCities.isEmpty()) {
					// Add any city as first city
					TSPCity anyCity = nameToCity.remove(nameToCity.keySet().iterator().next());

					orderedCities.add(anyCity);
				} else {
					TSPCity currentCity = orderedCities.get(orderedCities.size() - 1);
					TSPCity nextCity = nameToCity.values()
							.stream()
							.min(Comparator.comparing(candidateNextCity -> distance(currentCity, candidateNextCity)))
							.get();

					orderedCities.add(nextCity);
					nameToCity.remove(nextCity.getName());
				}
			}

			List<String> cityNames = orderedCities.stream().map(c -> c.getName()).collect(Collectors.toList());
			greedyMove = TSPSolution.builder().cities(cityNames).build();
		}

		return Map.of("lexicographical", lexicographicalMove, "greedy", greedyMove, "random", randomMove);
	}

	@Override
	public boolean isGameover(IKumiteBoard board) {
		return false;
	}
}
