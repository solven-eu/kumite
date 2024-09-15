package eu.solven.kumite.user;

import java.util.Map;
import java.util.Optional;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;

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
