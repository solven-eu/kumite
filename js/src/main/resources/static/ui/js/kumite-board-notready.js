import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

export function isReady(gameId, contestId) {
	const store = useKumiteStore();

	if (!store.games[gameId] || store.games[gameId].error) {
		return false;
	}

	if (!store.contests[contestId] || store.contests[contestId].error) {
		return false;
	}

	if (!store.contests[contestId].board || store.contests[contestId].board.error) {
		return false;
	}

	return true;
}

export default {
	components: {},
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
		...mapState(useUserStore, ["playingPlayerId"]),
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching", "nbBoardFetching"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId] || { error: "not_loaded" };
			},
			contest(store) {
				return store.contests[this.contestId] || { error: "not_loaded" };
			},
			board(store) {
				return store.contests[this.contestId]?.board || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();
		const userStore = useUserStore();

		return {};
	},
	// https://stackoverflow.com/questions/7717929/how-do-i-get-pre-style-overflow-scroll-height-150px-to-display-at-parti
	template: /* HTML */ `
        <div v-if="(!game || !contest || !board)">
            <div class="spinner-border" role="status" v-if="(nbGameFetching > 0 || nbContestFetching > 0 || nbBoardFetching > 0)">
                <span class="visually-hidden">Loading board for contestId={{contestId}}</span>
            </div>
            <div v-else>
                <span>Issue loading board for contestId={{contestId}}</span>
            </div>
        </div>
        <div v-if="game.error">game.error={{game.error}}</div>
        <div v-if="contest.error">contest.error={{contest.error}}</div>
        <div v-if="board.error">board.error={{board.error}}</div>
    `,
};
