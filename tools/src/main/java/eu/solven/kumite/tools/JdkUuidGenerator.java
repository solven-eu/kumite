package eu.solven.kumite.tools;

import java.util.UUID;

public class JdkUuidGenerator implements IUuidGenerator {

	public static final IUuidGenerator INSTANCE = new JdkUuidGenerator();

	@Override
	public UUID randomUUID() {
		return UUID.randomUUID();
	}

}
