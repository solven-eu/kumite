import { watch } from "vue";

import { defineStore } from "pinia";

import { useKumiteStore } from "./store.js";

export const usePlayersStore = defineStore("players", {
	state: () => ({
		nbLoading: 0,
		nbJoining: 0,
		playerStatuses: {},
	}),
	getters: {
		// isLoggedIn: (store) => Object.keys(store.account.raw).length > 0,
	},
	actions: {
		// Typically useful when an error is wrapped in the store
		onSwallowedError(error) {
			if (error instanceof NetworkError) {
				console.warn("An NetworkError is not being rethrown", error, error.response.status);
			} else {
				console.error("An Error is not being rethrown", error);
			}
		},

		async loadPlayingStatus(gameId, contestId) {
			const store = useKumiteStore();
			const playersStore = this;

			playersStore.nbLoading++;
			store
				.loadBoard(gameId, contestId)
				.then((contestView) => {
					console.debug("contestView", contestView);

					const playerStatus = contestView.playerStatus;

					playersStore.$patch({
						playerStatuses: { ...playersStore.playerStatuses, [contestId]: playerStatus },
					});
				})
				.finally(() => {
					playersStore.nbLoading--;
				});
		},

		async joinAsPlayer(playerId, contestId) {
			const store = useKumiteStore();
			const playersStore = this;

			async function fetchFromUrl(url) {
				try {
					const response = await store.authenticatedFetch(url, {
						method: "POST",
					});
					if (!response.ok) {
						throw new Error("Rejected request for joining as player url " + url);
					}
					const playerStatus = await response.json();

					playersStore.$patch({
						playerStatuses: { ...playersStore.playerStatuses, [contestId]: playerStatus },
					});

					// We registered a player: the contest is stale
					store.contests[contestId].stale = true;

					return playerStatus;
				} catch (e) {
					// console.error("Issue on Network: ", e);
					// isJoiningAsPlayer.value = false;
					// hasJoinedAsPlayer.value = false;
					throw e;
				}
			}

			playersStore.nbJoining++;
			return fetchFromUrl(`/board/player?player_id=${playerId}&contest_id=${contestId}&viewer=false`).finally(() => {
				playersStore.nbJoining--;
			});
		},
	},
});
