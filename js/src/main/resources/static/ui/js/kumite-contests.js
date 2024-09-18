import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import KumiteContest from "./kumite-contest.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteContest,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		gameId: {
			type: String,
			// required: true,
		},
        showGame: {
            type: Boolean,
            // As we show multiple contests, we do not show the game (by default)
            default: false,
        },
        showLeaderboard: {
            type: Boolean,
            // As we show multiple contests, we do not show the leaderboard (by default)
            default: false,
        },
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching"]),
		...mapState(useKumiteStore, {
			contests(store) {
				const allContests = Object.values(store.contests);

				console.debug("allContests", allContests);

				if (this.gameId) {
					// https://stackoverflow.com/questions/69091869/how-to-filter-an-array-in-array-of-objects-in-javascript
					return allContests.filter(
						(contest) => contest.constantMetadata.gameId === this.gameId,
					);
				} else {
					return allContests;
				}
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		if (props.gameId) {
			// The contests of a specific game
			store.loadContests(props.gameId);
		} else {
			// Cross-through contests
			store.loadContests();
		}

		return {};
	},

	template: /* HTML */ `
        <div v-if="Object.values(contests).length == 0 && nbContestFetching > 0">Loading contests</div>
        <div v-else class="container">
            <div class="row border" v-for="contest in contests">
                <KumiteContest :gameId="contest.constantMetadata.gameId" :contestId="contest.contestId" :showGame="showGame" :showLeaderboard="showLeaderboard" />
            </div>
        </div>
    `,
};
