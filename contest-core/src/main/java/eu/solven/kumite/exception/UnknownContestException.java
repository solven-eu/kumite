package eu.solven.kumite.exception;

import java.util.UUID;

public class UnknownContestException extends IllegalArgumentException {
	private static final long serialVersionUID = 8255557215046843558L;

	public UnknownContestException(UUID contestId) {
		super("No contest for contestId=" + contestId);
	}
}
