package eu.solven.kumite.account;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This is kind-of a {@link Map} from accountId to {@link KumiteUserRawRaw}.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteUserRawRawRepository {
	void putIfAbsent(UUID accountId, KumiteUserRawRaw rawRaw);

	Optional<KumiteUserRawRaw> getUser(UUID accountId);

}
