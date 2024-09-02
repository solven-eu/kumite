import { defineStore } from "pinia";

class NetworkError extends Error {
	constructor(message, url, response) {
		super(message);
		this.name = this.constructor.name;

		this.url = url;
		this.response = response;
	}
}

export const useKumiteStore = defineStore("kumite", {
	state: () => ({
		// Various metadata to enrich the UX
		metadata: {},

		// The loaded games and contests
		games: {},
		contests: {},
		boards: {},
		nbGameFetching: 0,
		nbContestFetching: 0,
		nbBoardFetching: 0,

		// Currently connected account
		account: {},

		// We loads information about various players (e.g. through leaderboards)
		players: {},
		nbAccountLoading: 0,
	}),
	getters: {
		defaultPlayerId: (state) => state.account.playerId,
	},
	actions: {
		async loadMetadata() {
			const store = this;

			async function fetchFromUrl(url) {
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected request for games url" + url,
							url,
							response,
						);
					}

					const responseJson = await response.json();
					const metadata = responseJson;

					store.$patch({ metadata: metadata });
				} catch (e) {
					console.error("Issue on Network: ", e);
				}
			}

			fetchFromUrl("/api/public/metadata");
		},

		async loadUser() {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected request for games url" + url,
							url,
							response,
						);
					}

					const responseJson = await response.json();
					const user = responseJson;

					store.$patch({ account: user });
				} catch (e) {
					console.error("Issue on Network: ", e);
					const user = { error: e };
					store.$patch({ account: user });
				} finally {
					store.nbAccountLoading--;
				}
			}

			fetchFromUrl("/api/private/user");
		},
		async loadCurrentAccountPlayers() {
			const store = this;

			if (!store.account.accountId) {
				// TODO What if `loadUser` was ongoing?
				console.log("Skip loading players are not account available");
				return;
			}

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new Error(
							"Rejected request for current account players" + url,
						);
					}

					const responseJson = await response.json();
					const user = responseJson;

					responseJson.forEach((player) => {
						console.log("Registering playerId", item.playerId);
						store.$patch({
							players: { ...store.players, [item.playerId]: item },
						});
					});
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbAccountLoading--;
				}
			}

			fetchFromUrl("/api/players?account_id=" + store.account.accountId);
		},
		async loadContestPlayers(contestId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new Error(
							"Rejected request for players of contest=" + contestId,
						);
					}

					const responseJson = await response.json();
					const user = responseJson;

					responseJson.forEach((player) => {
						console.log("Registering playerId", item.playerId);
						store.$patch({
							players: { ...store.players, [item.playerId]: item },
						});
					});
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbAccountLoading--;
				}
			}

			fetchFromUrl("/api/players?contest_id=" + contestId);
		},

		async loadGames(gameId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbGameFetching++;
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for games url" + url);
					}
					const responseJson = await response.json();

					responseJson.forEach((item) => {
						console.log("Registering gameId", item.gameId);
						store.$patch({ games: { ...store.games, [item.gameId]: item } });
					});
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbGameFetching--;
				}
			}

			fetchFromUrl("/api/games");
		},
		async loadGameIfMissing(gameId) {
			if (this.games[gameId]) {
				console.log("Skip loading gameId=", gameId);
			} else {
				console.log("About to load gameId", gameId);

				const store = this;

				async function fetchFromUrl(url) {
					store.nbGameFetching++;
					try {
						const response = await fetch(url);
						if (!response.ok) {
							throw new Error("Rejected request for gameId=" + gameId);
						}

						const responseJson = await response.json();

						if (responseJson.length !== 1) {
							console.error("We expected a single game", responseJson);
						}

						const game = responseJson[0];

						// https://github.com/vuejs/pinia/discussions/440
						console.log("Registering gameId", gameId);
						store.$patch({ games: { ...store.games, [gameId]: game } });
					} catch (e) {
						console.error("Issue on Fetch: ", e);

						const game = { gameId: gameId, status: "error", error: e };
						store.$patch({ games: { ...store.games, [gameId]: game } });
					} finally {
						store.nbGameFetching--;
					}
				}
				fetchFromUrl("/api/games?game_id=" + gameId);
			}
		},

		async loadContests(gameId) {
			const store = this;
			async function fetchFromUrl(url) {
				store.nbContestFetching++;
				try {
					const response = await fetch(url);
					const responseJson = await response.json();

					responseJson.forEach((item) => {
						console.log("Registering contestId", item.contestId);
						store.$patch({
							contests: { ...store.contests, [item.contestId]: item },
						});
					});
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbGameFetching--;
				}
			}

			if (gameId) {
				// The contests of a specific game
				fetchFromUrl("/api/contests?game_id=" + gameId);
			} else {
				// Cross-through contests
				fetchFromUrl("/api/contests");
			}
		},

		async loadContestIfMissing(gameId, contestId) {
			this.loadGameIfMissing(gameId);

			if (this.contests[contestId]) {
				console.log("Skip loading contestId=", contestId);
			} else {
				console.log("About to load contestId", contestId);

				const store = this;

				async function fetchFromUrl(url) {
					store.nbContestFetching++;
					try {
						const response = await fetch(url);
						if (!response.ok) {
							throw new NetworkError(
								"Rejected request for contest: " + contestId,
								url,
								response,
							);
						}

						const responseJson = await response.json();

						if (responseJson.length === 0) {
							throw new Error("Unknown contestId: " + contestId);
						} else if (responseJson.length !== 1) {
							console.error("We expected a single contest", responseJson);
						}

						const contest = responseJson[0];

						// https://github.com/vuejs/pinia/discussions/440
						console.log("Registering contestId", contestId);
						store.$patch({
							contests: { ...store.contests, [contestId]: contest },
						});
					} catch (e) {
						if (e instanceof NetworkError) {
							console.error("Issue on Fetch: ", e, e.response.status);
						} else {
							console.error("Issue on Fetch: ", e);
						}

						const contest = { contestId: contestId, status: "error", error: e };
						store.$patch({
							contests: { ...store.contests, [contestId]: contest },
						});
					} finally {
						store.nbContestFetching--;
					}
				}
				fetchFromUrl(
					"/api/contests?game_id=" + gameId + "&contest_id=" + contestId,
				);
			}
		},

		async loadBoard(gameId, contestId) {
			this.loadGameIfMissing(gameId);
			this.loadContestIfMissing(gameId, contestId);

			const store = this;

			async function fetchFromUrl(url) {
				store.nbBoardFetching++;
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected request for contest: " + contestId,
							url,
							response,
						);
					}

					const responseJson = await response.json();

					const board = responseJson;

					// https://github.com/vuejs/pinia/discussions/440
					console.log("Registering board for contestId", contestId);
					store.$patch({ boards: { ...store.boards, [contestId]: board } });
				} catch (e) {
					console.error("Issue on Fetch: ", e, e.response.status);

					const board = { contestId: contestId, status: "error", error: e };
					store.$patch({ boards: { ...store.boards, [contestId]: board } });
				} finally {
					store.nbBoardFetching--;
				}
			}
			const viewingPlayerId = "00000000-0000-0000-0000-000000000000";
			const playerId = viewingPlayerId;
			fetchFromUrl(
				"/api/board?game_id=" +
					gameId +
					"&contest_id=" +
					contestId +
					"&player_id=" +
					playerId,
			);
		},
	},
});
