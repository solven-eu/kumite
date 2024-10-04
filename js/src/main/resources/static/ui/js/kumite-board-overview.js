import { computed } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

import KumiteGameHeader from "./kumite-game-header.js";
import KumiteContestHeader from "./kumite-contest-header.js";

import KumiteBoardOverviewInner from "./kumite-board-overview-inner.js";

import KumiteBoardNotReady from "./kumite-board-notready.js";
import { isReady } from "./kumite-board-notready.js";

export default {
	components: {
		KumiteGameHeader,
		KumiteContestHeader,

		KumiteBoardOverviewInner,
		KumiteBoardNotReady,
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
	},
	setup(props) {
		const store = useKumiteStore();
		const userStore = useUserStore();

		// We load current accountPlayers to enable playingPlayerId
		userStore
			.loadCurrentAccountPlayers()
			.then(() => {
				if (userStore.playingPlayerId) {
					return store.loadBoard(props.gameId, props.contestId, userStore.playingPlayerId);
				} else {
					console.log("Skip loading board as seemingly not logged-in");
				}
			})
			.then((board) => {
				console.log("We loaded board", board);
			});

		const weAreReady = computed(() => isReady(props.gameId, props.contestId));

		return { weAreReady };
	},
	// https://stackoverflow.com/questions/7717929/how-do-i-get-pre-style-overflow-scroll-height-150px-to-display-at-parti
	template: /* HTML */ `
        <KumiteBoardNotReady v-if="!weAreReady" :gameId="gameId" :contestId="contestId" />
        <div v-else class="container">
            <KumiteGameHeader class="row" :gameId="gameId" />
            <KumiteContestHeader class="row" :gameId="gameId" :contestId="contestId" />
            <KumiteBoardOverviewInner class="row" :gameId="gameId" :contestId="contestId" />
        </div>
    `,
};
