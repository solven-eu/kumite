import { ref, watch, onMounted, onUnmounted } from "vue";

import { mapState, storeToRefs } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
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
	methods: {
		onMove: function (gameId, contestId) {
			console.log("onMove", gameId, contestId);
		},
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadLeaderboard(props.gameId, props.contestId);

		watch(
			() => store.leaderboards[props.contestId]?.stale,
			(newValue) => {
				console.log("Detected stale leaderboard", props.contestId);
				store.loadLeaderboard(props.gameId, props.contestId);
			},
		);

		const shortPollLeaderboardInterval = ref(null);

		function clearShortPollLeaderboard() {
			if (shortPollLeaderboardInterval.value) {
				console.log("Cancelling setInterval", "clearShortPollLeaderboard");
				clearInterval(shortPollLeaderboardInterval.value);
				shortPollLeaderboardInterval.value = null;
			}
		}

		/*
		 * Polling the contest status every 5seconds.
		 * The output can be used to cancel the polling.
		 */
		function shortPollLeaderboard() {
			// Cancel any existing related setInterval
			clearShortPollLeaderboard();

			const nextInterval = setInterval(() => {
				console.log("Intervalled shortPollLeaderboard", props.contestId);
				store.loadLeaderboard(props.gameId, props.contestId);
			}, 5000);
			shortPollLeaderboardInterval.value = nextInterval;

			return nextInterval;
		}

		onMounted(() => {
			// Update the leaderboard regularly
			shortPollLeaderboard();
		});

		onUnmounted(() => {
			clearShortPollLeaderboard();
		});

		return {};
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
	<i class="bi bi-speedometer"></i>
	<div v-if="leaderboard.playerScores && leaderboard.playerScores.length">
		<li v-for="item in leaderboard.playerScores">
		  {{item.playerId}} has score {{item.score}}
		</li>
	</div>
	<span v-else>
		Leaderboard is empty
	</span>
</div>
  `,
};
