package eu.solven.kumite.board;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BoardAndMetadata implements IHasBoardAndMetadata {
	IKumiteBoard board;
	BoardDynamicMetadata metadata;

}
