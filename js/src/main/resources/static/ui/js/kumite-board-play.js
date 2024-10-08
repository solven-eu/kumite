import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

import KumiteGameHeader from "./kumite-game-header.js";
import KumiteContestHeader from "./kumite-contest-header.js";

import KumiteBoardPlayInner from "./kumite-board-play-inner.js";

export default {
	components: {
		KumiteGameHeader,
		KumiteContestHeader,

		KumiteBoardPlayInner,
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

		// We load current accountPlayers to enable playingPlayerId
		userStore
			.loadCurrentAccountPlayers()
			.then(() => {
				return store.loadBoard(props.gameId, props.contestId, userStore.playingPlayerId);
			})
			.then((board) => {
				console.log("We loaded board", board);
			});

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
        <div v-else-if="game.error || contest.error || board.error">{{game.error || contest.error || board.error}}</div>
        <div v-else class="container">
            <KumiteGameHeader class="row" :gameId="gameId" />
            <KumiteContestHeader class="row" :gameId="gameId" :contestId="contestId" />
            <KumiteBoardPlayInner class="row border" :gameId="gameId" :contestId="contestId" />
        </div>
    `,
};
