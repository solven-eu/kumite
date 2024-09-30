import { ref, watch, computed } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";
import { usePlayersStore } from "./store-players.js";

import KumiteContestFormRef from "./kumite-contest-form-ref.js";

export default {
	components: {
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
	},
	computed: {
		...mapState(useKumiteStore, {
			contest(store) {
				return store.contests[this.contestId] || { error: "not_loaded" };
			},
		}),
		...mapState(usePlayersStore, ["nbJoining"]),
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
		const userStore = useUserStore();
		const playersStore = usePlayersStore();

		function joinAsPlayer() {
			const playerId = userStore.playingPlayerId;
			const contestId = props.contestId;

			return playersStore.joinAsPlayer(playerId, contestId);
		}

		function joinAsViewer() {
			throw Error("Not implemented yet");
		}

		const isLoading = ref(true);
		playersStore.loadPlayingStatus(props.gameId, props.contestId).then(() => {
			isLoading.value = false;
		});

		const contest = ref(store.contests[props.contestId]);
		const requiringPlayers = computed(() => contest.value.dynamicMetadata.requiringPlayers);

		return {
			isLoading,

			joinAsPlayer,
			joinAsViewer,

			requiringPlayers,
		};
	},
	template: /* HTML */ `
        <div>
            <!-- Loading metadata -->
            <div v-if="isLoading">Loading {{isLoading}}</div>
            <!-- IsJoining loader -->
            <div v-else-if="nbJoining > 0">Joining...</div>
            <!-- Joined as player -->
            <div v-else-if="playerHasJoined">
                <div v-if="requiringPlayers">
                    Waiting for more players ({{contest.dynamicMetadata.contenders.length}} / {{ contest.constantMetadata.minPlayers }})
                </div>
                <!-- Can be played -->
                <div v-else-if="contest.dynamicMetadata.gameOver">
                    Game Over<br />

                    <KumiteContestFormRef :gameId="gameId" />
                </div>
                <div v-else>
                    <RouterLink :to="{ name: 'play'}">
                        <button class="btn btn-primary"><i class="bi bi-controller"></i>Play a move</button>
                    </RouterLink>
                </div>
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
