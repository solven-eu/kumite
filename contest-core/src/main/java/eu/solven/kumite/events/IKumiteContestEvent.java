package eu.solven.kumite.events;

import java.util.UUID;

import eu.solven.kumite.contest.Contest;

/**
 * Tag a event as being related to the {@link Contest} lifecycle.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteContestEvent {

	UUID getContestId();

}
