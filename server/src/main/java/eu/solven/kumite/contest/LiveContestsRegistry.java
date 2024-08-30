package eu.solven.kumite.contest;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * An {@link IContest} may start and end. Once ended, it can not resume itself.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Slf4j
public class LiveContestsRegistry {
	Set<UUID> liveContests = new ConcurrentSkipListSet<UUID>();

	public boolean isContestLive(UUID contestId) {
		return liveContests.contains(contestId);
	}

	public void registerContestLive(UUID contestId) {
		boolean added = liveContests.add(contestId);
		if (added) {
			log.info("contestId={} turned live", contestId);
		} else {
			throw new IllegalArgumentException("contestId=" + contestId + " is already live");
		}
	}

	public void registerContestOver(UUID contestId) {
		boolean removed = liveContests.remove(contestId);

		if (removed) {
			log.info("contestId={} turned over", contestId);
		} else {
			throw new IllegalArgumentException("contestId=" + contestId + " is not live");
		}
	}
}
