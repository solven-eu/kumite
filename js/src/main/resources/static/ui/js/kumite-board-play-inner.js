import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { usePlayersStore } from "./store-players.js";

import KumiteBoardPlayInnerJoined from "./kumite-board-play-inner-joined.js";

export default {
	components: {
		KumiteBoardPlayInnerJoined,
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
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching", "nbBoardFetching", "playingPlayerId"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId] || { error: "not_loaded" };
			},
			contest(store) {
				return store.contests[this.contestId] || { error: "not_loaded" };
			},
			board(store) {
				return store.contests[this.contestId].board || { error: "not_loaded" };
			},
		}),
		...mapState(usePlayersStore, {
			playerCanJoin(store) {
				return store.playerStatuses[this.contestId]?.playerCanJoin;
			},
			playerHasJoined(store) {
				return store.playerStatuses[this.contestId]?.playerHasJoined;
			},
			isJoiningAsPlayer(store) {
				return store.playerStatuses[this.contestId]?.isJoiningAsPlayer;
			},

			accountIsViewing(store) {
				return store.playerStatuses[this.contestId]?.accountIsViewing;
			},
			isJoiningAsViewer(store) {
				return store.playerStatuses[this.contestId]?.isJoiningAsViewer;
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();
		const playersStore = usePlayersStore();

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		function joinAsPlayer() {
			const playerId = store.playingPlayerId;
			const contestId = props.contestId;

			return playersStore.joinAsPlayer(playerId, contestId);
		}

		function joinAsViewer() {
			throw Error("Not implemented yet");
		}

		const isLoading = ref(false);
		playersStore.loadPlayingStatus(props.gameId, props.contestId).then(() => {
			isLoading.value = false;
		});

		return {
			playersStore,
			isLoading,

			joinAsPlayer,
			joinAsViewer,
		};
	},
	template: /* HTML */ `
        <div class="border" v-if="contest">
            <!-- Loading metadata -->
            <div v-if="isLoading">Loading {{isLoading}}</div>
            <!-- IsJoining loader -->
            <div v-else-if="isJoiningAsPlayer || isJoiningAsViewer">Joining {{isJoiningAsPlayer || isJoiningAsViewer}}</div>
            <!-- Joined as player -->
            <div v-else-if="playerHasJoined">
                <KumiteBoardPlayInnerJoined :gameId="gameId" :contestId="contestId" />
            </div>
            <!-- Joined as viewer -->
            <div v-else-if="accountIsViewing">accountIsViewing={{accountIsViewing}}</div>
            <!-- Joining options -->
            <div v-else>
                <!-- Offer to join as player-->
                <div v-if="playerCanJoin">
                    <button type="button" @click="joinAsPlayer()" class="btn btn-outline-primary">Join contest as player</button>
                </div>
                <!-- Offer to join as viewer -->
                <div>
                    <button type="button" @click="joinAsViewer()" class="btn btn-outline-primary" disabled>Join contest as viewer</button>
                </div>
            </div>
        </div>
    `,
};
