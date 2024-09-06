package eu.solven.kumite.tools;

import java.util.UUID;

/**
 * Wraps the {@link UUID} generation logic. It can be useful to have deterministic UUID generation for local
 * environments (rebooting frequently).
 * 
 * @author Benoit Lacelle
 *
 */
// https://github.com/cowtowncoder/java-uuid-generator
public interface IUuidGenerator {
	UUID randomUUID();
}
