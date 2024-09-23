package eu.solven.kumite.account;

import java.util.Map;
import java.util.Optional;

/**
 * This is kind-of a {@link Map} from {@link KumiteUserRawRaw} to {@link KumiteUser}.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteUserRepository {
	Optional<KumiteUser> getUser(KumiteUserRawRaw accountId);

	KumiteUser registerOrUpdate(KumiteUserRaw kumiteUserRaw);
}
