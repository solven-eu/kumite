package eu.solven.kumite.game.optimization.tsp;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoardView;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = { "boardSvg", "moveSvg" }, allowGetters = true)
public class TSPProblem implements IKumiteBoardView {
	// The unordered set of cities waiting to be visited
	@Singular
	Set<TSPCity> cities;

	// This property has to be sent to the UI, but there is no point in reading-it back
	// @JsonProperty(access = Access.READ_ONLY)
	@Override
	public String getBoardSvg() {
		return "KumiteTSPBoardState";
	}

	@Override
	public String getMoveSvg() {
		return "KumiteTSPBoardMove";
	}
}
