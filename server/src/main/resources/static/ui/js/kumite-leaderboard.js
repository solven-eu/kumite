import { ref, watch } from "vue";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	setup(props) {
		const store = useKumiteStore();

		store.loadLeaderboard(props.gameId, props.contestId);

		const leaderboard = ref(store.leaderboards[props.contestId]);

		watch(
			() => leaderboard.stale,
			(newValue) => {
				console.log("Detected stale leaderboard", contestId);
				store.loadLeaderboard(props.gameId, props.contestId);
			},
		);

		return {};
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
	},
	computed: {
		...mapState(useKumiteStore, [
			"nbGameFetching",
			"nbContestFetching",
			"nbLeaderboardFetching",
		]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId] || { error: "not_loaded" };
			},
			contest(store) {
				return store.contests[this.contestId] || { error: "not_loaded" };
			},
			leaderboard(store) {
				return (
					store.leaderboards[this.contestId] || {
						error: "not_loaded",
					}
				);
			},
		}),
	},
	template: `
<div v-if="(!game || !contest || !leaderboard) && (nbGameFetching > 0 || nbContestFetching > 0 || nbLeaderboardFetching > 0)">
	<div class="spinner-border" role="status">
	  <span class="visually-hidden">Loading leaderboard for contestId={{contestId}}</span>
	</div>
</div>
<div v-else-if="game.error || contest.error || leaderboard.error">
	{{game.error || contest.error || leaderboard.error}}
</div>
<div v-else>
	{{leaderboard}}
	<div v-if="leaderboard.playerScores && leaderboard.playerScores.length">
	leaderboard={{leaderboard}}
	<li v-for="item in leaderboard.playerScores">
	  {{item}}
	</li>
	</div>
	<div v-else>
		Leaderboard is empty
	</div>
</div>
  `,
};
