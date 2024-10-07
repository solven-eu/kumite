package eu.solven.kumite.board;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class KumiteBoardViewWrapper implements IKumiteBoardViewWrapper {
	UUID boardStateId;
	IKumiteBoardView view;
}
