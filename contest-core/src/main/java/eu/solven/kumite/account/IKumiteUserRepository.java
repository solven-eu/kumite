package eu.solven.kumite.account;

import java.util.Map;
import java.util.Optional;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;

/**
 * This is kind-of a {@link Map} from {@link KumiteUserRawRaw} to {@link KumiteUser}.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteUserRepository {
	Optional<KumiteUser> getUser(KumiteUserRawRaw accountId);

	KumiteUser registerOrUpdate(KumiteUserPreRegister kumiteUserPreRegister);
}
