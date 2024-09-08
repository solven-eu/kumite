import { ref, onMounted, onUnmounted } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteBoardMoveForm from "./kumite-board-move-form.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteBoardMoveForm,
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
		showCurl: {
			type: Boolean,
			default: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, [
			"nbGameFetching",
			"nbContestFetching",
			"nbBoardFetching",
			"playingPlayerId",
		]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
			board(store) {
				return store.boards[this.contestId];
			},
		}),
		curlGetBoard() {
			return (
				"curl " +
				window.location.protocol +
				"//" +
				window.location.host +
				"/api/board?contest_id=" +
				this.contestId
			);
		},
	},
	setup(props) {
		const store = useKumiteStore();

		const game = ref(store.games[props.gameId]);
		const contest = ref(store.contests[props.contestId]);

		const requiringPlayers = ref(
			contest.value.dynamicMetadata.requiringPlayers,
		);

		const shortPollBoardInterval = ref(null);

		function clearShortPollBoard() {
			if (shortPollBoardInterval.value) {
				console.log("Cancelling setInterval", "clearShortPollBoard");
				clearInterval(shortPollBoardInterval.value);
				shortPollBoardInterval.value = null;
			}
		}

		/*
		 * Polling the contest status every 5seconds.
		 * The output can be used to cancel the polling.
		 */
		function shortPollBoard(playerId) {
			// Cancel any existing related setInterval
			clearShortPollBoard();

			const nextInterval = setInterval(() => {
				console.log("Intervalled shortPollBoard");
				store.loadBoard(props.gameId, props.contestId, playerId);
			}, 5000);
			shortPollBoardInterval.value = nextInterval;

			return nextInterval;
		}

		onMounted(() => {
			// We short poll the contestView to update its status like `requiringPlayers` or `gameOver`
			shortPollBoard(store.playingPlayerId);
		});

		onUnmounted(() => {
			clearShortPollBoard();
		});

		return {
			requiringPlayers,
		};
	},
	template: `
	<div class="border" v-if="contest">
	   <!-- Waiting for players -->
	   <div v-if="requiringPlayers">
	      Waiting for more players ({{contest.dynamicMetadata.players.length}} / {{ contest.constantMetadata.minPlayers }})
	   </div>
	   <!-- Can be played -->
	   <div v-else>
	      <KumiteBoardMoveForm :gameId="gameId" :contestId="contestId" />
	   </div>
	</div>
  `,
};
