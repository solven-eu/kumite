package eu.solven.kumite.player;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Value
@Builder
public class PlayersList implements IHasPlayers {
	@Singular
	List<KumitePlayer> players;
}
