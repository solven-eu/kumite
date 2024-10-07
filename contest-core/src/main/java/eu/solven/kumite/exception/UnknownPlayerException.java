package eu.solven.kumite.exception;

import java.util.UUID;

public class UnknownPlayerException extends IllegalArgumentException {
	private static final long serialVersionUID = 8255557215046843558L;

	public UnknownPlayerException(UUID playerId) {
		super("No contest for playerId=" + playerId);
	}
}
