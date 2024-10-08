import { ref, computed, onMounted, onUnmounted } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteBoardMoveForm from "./kumite-board-move-form.js";
import KumiteContestFormRef from "./kumite-contest-form-ref.js";

export default {
	components: {
		KumiteBoardMoveForm,
		KumiteContestFormRef,
	},
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
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching", "nbBoardFetching", "playingPlayerId"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
			board(store) {
				return store.contests[this.contestId]?.board;
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		const contest = ref(store.contests[props.contestId]);

		// https://github.com/vuejs/core/issues/5818
		const requiringPlayers = computed(() => contest.value.dynamicMetadata.requiringPlayers);

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

			const intervalPeriodMs = 50000;
			console.log("setInterval", "shortPollBoard", intervalPeriodMs);

			const nextInterval = setInterval(() => {
				console.log("Intervalled shortPollBoard", props.contestId, playerId);
				store.loadBoard(props.gameId, props.contestId, playerId);
			}, intervalPeriodMs);
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

		return { contest, requiringPlayers };
	},
	template: /* HTML */ `
        <div class="border" v-if="contest">
            <!-- Waiting for players -->
            <div v-if="requiringPlayers">
                Waiting for more players ({{contest.dynamicMetadata.contenders.length}} / {{ contest.constantMetadata.minPlayers }})
            </div>
            <!-- Can be played -->
            <div v-else-if="contest.dynamicMetadata.gameOver">
                Game Over<br />

                <KumiteContestFormRef :gameId="gameId" />
            </div>
            <div v-else>
                <KumiteBoardMoveForm :gameId="gameId" :contestId="contestId" />
            </div>
        </div>
    `,
};
