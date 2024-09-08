import { ref, onMounted, onUnmounted } from "vue";

import { mapState } from "pinia";
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

		const shortPollContestDynamicInterval = ref(null);

		function clearShortPollContestDynamic() {
			if (shortPollContestDynamicInterval.value) {
				console.log("Cancelling setInterval");
				clearInterval(shortPollContestDynamicInterval.value);
				shortPollContestDynamicInterval.value = null;
			}
		}

		/*
		 * Polling the contest status every 5seconds.
		 * The output can be used to cancel the polling.
		 */
		function shortPollContestDynamic() {
			// Cancel any existing related setInterval
			clearShortPollContestDynamic();

			const nextInterval = setInterval(() => {
				console.log("Intervalled shortPollContestDynamic");
				store.loadContest(props.gameId, props.contestId);
			}, 5000);
			shortPollContestDynamicInterval.value = nextInterval;

			return nextInterval;
		}

		onMounted(() => {
			shortPollContestDynamic();
		});

		onUnmounted(() => {
			clearShortPollContestDynamic();
		});

		store
			.loadContestIfMissing(props.gameId, props.contestId)
			.then((contest) => {});

		return {};
	},
	template: `
<div v-if="(!game || !contest) && (nbGameFetching > 0 || nbContestFetching > 0)">
	<div class="spinner-border" role="status">
	  <span class="visually-hidden">Loading contestId={{contestId}}</span>
	</div>
</div>
<div v-else-if="game.error || contest.error">
	{{game.error || contest.error}}
</div>
<span v-else>
	<h2>
		<RouterLink :to="{path:'/html/games/' + gameId + '/contest/' + contestId}"><i class="bi bi-trophy"></i> {{contest.constantMetadata.name}}</RouterLink>
		<RouterLink :to="{path:'/html/games/' + gameId}"><i class="bi bi-arrow-90deg-left"></i></RouterLink>
	</h2>
</span>
  `,
};
