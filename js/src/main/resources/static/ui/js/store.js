import { watch } from "vue";

import { defineStore } from "pinia";

import { useUserStore } from "./store-user.js";

class NetworkError extends Error {
	constructor(message, url, response) {
		super(message);
		this.name = this.constructor.name;

		this.url = url;
		this.response = response;
	}
}

const prefix = "/api/v1";

export const useKumiteStore = defineStore("kumite", {
	state: () => ({
		// Various metadata to enrich the UX
		metadata: {},

		// The loaded games and contests
		games: {},
		contests: {},
		nbGameFetching: 0,
		nbContestFetching: 0,
		nbBoardFetching: 0,

		// We loads information about various players (e.g. current account, through contests and leaderboards)
		// Playing players are stores in contests
		players: {},
		nbPlayersLoading: 0,

		// Typically edited when a player submit a move
		leaderboards: {},
		nbLeaderboardFetching: 0,

		// Relates to POST
		nbBoardOperating: 0,
	}),
	actions: {
		// Typically useful when an error is wrapped in the store
		onSwallowedError(error) {
			if (error instanceof NetworkError) {
				console.warn("An NetworkError is not being rethrown", error, error.response.status);
			} else {
				console.error("An Error is not being rethrown", error);
			}
		},
		newNetworkError(msg, url, response) {
			return new NetworkError("Rejected request for games url" + url, url, response);
		},

		async loadMetadata() {
			const store = this;

			async function fetchFromUrl(url) {
				const response = await fetch(url);
				if (!response.ok) {
					throw new NetworkError("Rejected request for games url" + url, url, response);
				}

				const responseJson = await response.json();
				const metadata = responseJson;

				store.$patch({ metadata: metadata });
			}

			return fetchFromUrl(prefix + "/public/metadata");
		},

		async loadPlayer(playerId) {
			const store = this;
			const userStore = useUserStore();

			async function fetchFromUrl(url) {
				store.nbPlayersLoading++;
				try {
					const response = await userStore.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for players of playerId=" + playerId);
					}

					const responseJson = await response.json();
					const players = responseJson;

					players.forEach((player) => {
						console.log("Storing playerId", player.playerId);
						store.$patch({
							players: { ...store.players, [player.playerId]: player },
						});
					});
				} catch (e) {
					store.onSwallowedError(e);
				} finally {
					store.nbPlayersLoading--;
				}
			}

			return fetchFromUrl(`/players?player_id=${playerId}`);
		},

		async loadContestPlayers(contestId) {
			const store = this;
			const userStore = useUserStore();

			async function fetchFromUrl(url) {
				store.nbPlayersLoading++;
				try {
					const response = await userStore.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for players of contest=" + contestId);
					}

					const responseJson = await response.json();
					const players = responseJson;

					players.forEach((player) => {
						console.log("Storing playerId", player.playerId);
						store.$patch({
							players: { ...store.players, [player.playerId]: player },
						});
					});

					console.log("Storing players for contestId", contestId, players);
					const mutatedContest = {
						...store.contests[contestId],
						players: players,
					};
					store.$patch({
						contests: {
							...store.contests,
							[contestId]: mutatedContest,
						},
					});
				} catch (e) {
					store.onSwallowedError(e);
				} finally {
					store.nbPlayersLoading--;
				}
			}

			return fetchFromUrl(`/players?contest_id=${contestId}`);
		},

		async loadGames() {
			const store = this;
			const userStore = useUserStore();

			async function fetchFromUrl(url) {
				store.nbGameFetching++;

				try {
					const response = await userStore.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for games url" + url);
					}
					const responseJson = await response.json();

					responseJson.forEach((item) => {
						console.log("Registering gameId", item.gameId);
						store.$patch({
							games: { ...store.games, [item.gameId]: item },
						});
					});
				} catch (e) {
					store.onSwallowedError(e);
				} finally {
					store.nbGameFetching--;
				}
			}

			return fetchFromUrl("/games");
		},
		async loadGameIfMissing(gameId) {
			if (this.games[gameId]) {
				console.debug("Already stored gameId=", gameId);

				return Promise.resolve(this.games[gameId]);
			} else {
				console.log("About to load gameId", gameId);

				const store = this;
				const userStore = useUserStore();

				async function fetchFromUrl(url) {
					store.nbGameFetching++;
					try {
						const response = await userStore.authenticatedFetch(url);
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
						store.$patch({
							games: { ...store.games, [gameId]: game },
						});

						return game;
					} catch (e) {
						store.onSwallowedError(e);

						const game = {
							gameId: gameId,
							error: e,
						};
						store.$patch({
							games: { ...store.games, [gameId]: game },
						});

						return game;
					} finally {
						store.nbGameFetching--;
					}
				}
				return fetchFromUrl("/games?game_id=" + gameId);
			}
		},

		async loadContests(gameId) {
			const store = this;
			const userStore = useUserStore();
			async function fetchFromUrl(url) {
				store.nbContestFetching++;
				try {
					const response = await userStore.authenticatedFetch(url);
					const responseJson = await response.json();

					console.debug("responseJson", responseJson);

					const contests = responseJson;
					contests.forEach((contest) => {
						console.log("Registering contestId", contest.contestId);
						store.$patch({
							contests: {
								...store.contests,
								[contest.contestId]: contest,
							},
						});
					});
					return contests;
				} catch (e) {
					store.onSwallowedError(e);
					return [];
				} finally {
					store.nbContestFetching--;
				}
			}

			let url = "/contests";
			if (gameId) {
				// The contests of a specific game
				url += "?game_id=" + gameId;
			}
			return fetchFromUrl(url);
		},

		mergeContest(contestUpdate) {
			const contestId = contestUpdate.contestId;
			// The contest may be empty on first load
			const oldContest = this.contests[contestId] || {};
			// This this property right-away as it is watched
			const mergedContest = {
				...oldContest,
				...contestUpdate,
				stale: false,
			};

			// BEWARE This is broken if we consider a user can manage multiple playerIds
			console.log("Storing board for contestId", contestId, mergedContest);
			this.$patch({
				contests: { ...this.contests, [contestId]: mergedContest },
			});

			return mergedContest;
		},

		async loadContest(gameId, contestId) {
			return this.loadGameIfMissing(gameId).then(() => {
				console.log("About to load/refresh contestId", contestId);

				const store = this;
				const userStore = useUserStore();

				async function fetchFromUrl(url) {
					store.nbContestFetching++;
					try {
						const response = await userStore.authenticatedFetch(url);
						if (!response.ok) {
							throw new NetworkError("Rejected request for contest: " + contestId, url, response);
						}

						const responseJson = await response.json();

						if (responseJson.length === 0) {
							return { contestId: contestId, error: "unknown" };
						} else if (responseJson.length !== 1) {
							// This should not happen as we provided an input contestId
							console.error("We expected a single contest", responseJson);
							return { contestId: contestId, error: "unknown" };
						}

						const contest = responseJson[0];

						return contest;
					} catch (e) {
						store.onSwallowedError(e);

						const contest = {
							contestId: contestId,
							error: e,
						};

						return contest;
					} finally {
						store.nbContestFetching--;
					}
				}
				return fetchFromUrl(`/contests?game_id=${gameId}&contest_id=${contestId}`).then((contest) => {
					return this.mergeContest(contest);
				});
			});
		},

		async loadContestIfMissing(gameId, contestId) {
			return this.loadGameIfMissing(gameId).then(() => {
				if (this.contests[contestId]) {
					console.debug("Skip loading contestId=", contestId);
					return Promise.resolve(this.contests[contestId]);
				} else {
					return this.loadContest(gameId, contestId);
				}
			});
		},

		async loadBoard(gameId, contestId, playerId) {
			console.debug("gameId", gameId);
			if (!playerId) {
				playerId = useUserStore().playingPlayerId;
			}
			if (!playerId) {
				throw new Error("playingPlayerId is undefined");
			}

			const store = this;
			const userStore = useUserStore();

			return this.loadContestIfMissing(gameId, contestId).then((contest) => {
				if (contest.error === "unknown") {
					return contest;
				}

				async function fetchFromUrl(url) {
					store.nbBoardFetching++;
					try {
						const response = await userStore.authenticatedFetch(url);
						if (!response.ok) {
							throw new NetworkError("Rejected request for board: " + contestId, url, response);
						}

						const responseJson = await response.json();
						const contestWithBoard = responseJson;

						return contestWithBoard;
					} catch (e) {
						store.onSwallowedError(e);

						return {
							contestId: contestId,
							error: e,
						};
					} finally {
						store.nbBoardFetching--;
					}
				}

				return fetchFromUrl(`/board?game_id=${gameId}&contest_id=${contestId}&player_id=${playerId}`).then((contestWithBoard) =>
					this.mergeContest(contestWithBoard),
				);
			});
		},

		async loadLeaderboard(gameId, contestId) {
			const store = this;
			const userStore = useUserStore();

			async function fetchFromUrl(url) {
				store.nbLeaderboardFetching++;
				try {
					const response = await userStore.authenticatedFetch(url);
					if (!response.ok) {
						throw new NetworkError("Rejected request for leaderboard: " + contestId, url, response);
					}

					const responseJson = await response.json();

					const leaderboard = responseJson;

					// We need to configure all object properties right-away
					// Else, `stale` would not be reset/removed by a fresh leaderboard (i.e. without `stale` property)
					// https://stackoverflow.com/questions/76709501/pinia-state-not-updating-when-using-spread-operator-object-in-patch
					// https://github.com/vuejs/pinia/issues/43
					leaderboard.stale = false;

					// https://github.com/vuejs/pinia/discussions/440
					console.log("Storing leaderboard for contestId", contestId);
					store.$patch({
						leaderboards: {
							...store.leaderboards,
							[contestId]: leaderboard,
						},
					});
				} catch (e) {
					store.onSwallowedError(e);

					const leaderboard = {
						contestId: contestId,
						error: e,
						stale: false,
					};
					store.$patch({
						leaderboards: {
							...store.leaderboards,
							[contestId]: leaderboard,
						},
					});
					return leaderboard;
				} finally {
					store.nbLeaderboardFetching--;
				}
			}

			return this.loadContestIfMissing(gameId, contestId).then(() => fetchFromUrl("/leaderboards?contest_id=" + contestId));
		},
	},
});
