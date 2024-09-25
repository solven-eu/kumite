import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteGameHeader from "./kumite-game-header.js";
import KumiteContestHeader from "./kumite-contest-header.js";

import KumiteLeaderboard from "./kumite-leaderboard.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteGameHeader,
		KumiteContestHeader,
		KumiteLeaderboard,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		contestId: {
			type: String,
			required: true,
		},
		gameId: {
			type: String,
			required: true,
		},
		showGame: {
			type: Boolean,
			default: true,
		},
		showLeaderboard: {
			type: Boolean,
			default: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadContestIfMissing(props.gameId, props.contestId);

		return {};
	},
	template: /* HTML */ `
        <div v-if="(!game || !contest)">
            <div v-if="(nbGameFetching > 0 || nbContestFetching > 0)">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Loading contestId={{contestId}}</span>
                </div>
            </div>
            <div v-else>
                <span>Issue loading contestId={{contestId}}</span>
            </div>
        </div>
        <div v-else-if="game.error || contest.error">{{game.error || contest.error}}</div>
        <div v-else>
            <KumiteContestHeader :gameId="gameId" :contestId="contestId" />

            <KumiteGameHeader :gameId="gameId" :withDescription="false" v-if="showGame" />

            <RouterLink :to="{path:'/html/games/' + gameId + '/contest/' + contestId + '/board'}">
                <button type="button" class="btn btn-outline-primary">Preview the board</button>
            </RouterLink>

            <KumiteLeaderboard :gameId="gameId" :contestId="contestId" v-if="showLeaderboard" />
        </div>
    `,
};
