import { watch } from "vue";

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
		tokens: {},

		// We loads information about various players (e.g. through leaderboards)
		// Playing players are stores in contests
		players: {},
		nbAccountLoading: 0,

		// Typically edited when a player submit a move
		leaderboards: {},
		nbLeaderboardFetching: 0,

		// Relates to POST
		nbBoardOperating: 0,
	}),
	getters: {
		// There will be a way to choose a different playerId amongst the account playerIds
		playingPlayerId: (store) => store.account.playerId,
		// Default headers: we authenticate ourselves
		apiHeaders: (store) => {
			if (store.needsToRefreshAccessToken) {
				// TODO Implement automated access_token refresh through Promise
				throw new Error("access_token is missing or expired");
			}
			return { Authorization: "Bearer " + store.tokens.access_token };
		},
		needsToRefreshAccessToken: (store) => {
			return !store.tokens.access_token || store.tokens.access_token_expired;
		},
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
					throw e;
				}
			}

			return fetchFromUrl("/api/public/v1/metadata");
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

			return fetchFromUrl("/api/login/v1/user");
		},

		async loadUserTokens() {
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
					const tokens = responseJson;

					{
						tokens.access_token_expired = false;
					}

					console.log("Tokens are stored");
					store.$patch({ tokens: tokens });

					watch(
						() => store.tokens.access_token_expired,
						(expired) => {
							if (expired) {
								console.log(
									"access_token is expired. Triggering loadUserTokens",
								);
								loadUserTokens();
							}
						},
					);

					return tokens;
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbAccountLoading--;
				}
			}

			return fetchFromUrl("/api/login/v1/token");
		},

		async loadIfMissingUserTokens() {
			if (this.tokens.access_token && !this.tokens.access_token_expired) {
				console.debug(
					"Authenticated and an access_tokenTokens is stored stored",
				);
			} else {
				await this.loadUserTokens();
			}

			return this.tokens;
		},
		async authenticatedFetch(url, fetchOptions) {
			await this.loadIfMissingUserTokens();

			const apiHeaders = this.apiHeaders;

			// fetchOptions are optional
			fetchOptions = fetchOptions || {};

			// https://stackoverflow.com/questions/171251/how-can-i-merge-properties-of-two-javascript-objects
			const mergeHeaders = Object.assign(
				{},
				apiHeaders,
				fetchOptions.headers || {},
			);

			const mergedFetchOptions = Object.assign({ method: "GET" }, fetchOptions);
			mergedFetchOptions.headers = mergeHeaders;

			console.debug("->", mergedFetchOptions.method, url, mergedFetchOptions);

			return fetch(url, mergedFetchOptions).then((response) => {
				console.debug(
					"<-",
					mergedFetchOptions.method,
					url,
					mergedFetchOptions,
					response,
				);

				if (response.status == 401) {
					this.tokens.access_token_expired = true;
				}

				return response;
			});
		},

		async loadCurrentAccountPlayers() {
			const store = this;

			if (!store.account.accountId) {
				// TODO What if `loadUser` was ongoing?
				return this.loadUser();
			}

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await store.authenticatedFetch(url);
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

			return fetchFromUrl("/api/players?account_id=" + store.account.accountId);
		},
		async loadContestPlayers(contestId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await store.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error(
							"Rejected request for players of contest=" + contestId,
						);
					}

					const responseJson = await response.json();
					const players = responseJson;

					//					responseJson.forEach((player) => {
					//						console.log("Registering playerId", player.playerId);
					//						store.$patch({
					//							players: { ...store.players, [player.playerId]: player },
					//						});
					//					});

					console.log("Storing players for contestId", contestId, players);
					const mutatedContest = {
						...store.contests[contestId],
						players: players,
					};
					store.$patch({
						contests: { ...store.contests, [contestId]: mutatedContest },
					});
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbAccountLoading--;
				}
			}

			return fetchFromUrl("/api/players?contest_id=" + contestId);
		},

		async loadGames(gameId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbGameFetching++;

				try {
					// console.log("Fetch headers:", store.apiHeaders);
					const response = await store.authenticatedFetch(url);
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

			return fetchFromUrl("/api/games");
		},
		async loadGameIfMissing(gameId) {
			if (this.games[gameId]) {
				console.log("Skip loading gameId=", gameId);

				return this.games[gameId];
			} else {
				console.log("About to load gameId", gameId);

				const store = this;

				async function fetchFromUrl(url) {
					store.nbGameFetching++;
					try {
						const response = await store.authenticatedFetch(url);
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

						return game;
					} catch (e) {
						console.error("Issue on Fetch: ", e);

						const game = { gameId: gameId, status: "error", error: e };
						store.$patch({ games: { ...store.games, [gameId]: game } });

						return game;
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
					const response = await store.authenticatedFetch(url);
					const responseJson = await response.json();

					console.log("responseJson", responseJson);

					responseJson.forEach((contest) => {
						console.log("Registering contestId", contest.contestId);
						store.$patch({
							contests: { ...store.contests, [contest.contestId]: contest },
						});
					});
				} catch (e) {
					console.error("Issue on Network: ", e);
				} finally {
					store.nbContestFetching--;
				}
			}

			let url = "/api/contests";
			if (gameId) {
				// The contests of a specific game
				url += "?game_id=" + gameId;
			}
			return fetchFromUrl(url);
		},

		async loadContest(gameId, contestId) {
			return this.loadGameIfMissing(gameId).then((game) => {
				console.log("About to load/refresh contestId", contestId);

				const store = this;

				async function fetchFromUrl(url) {
					store.nbContestFetching++;
					try {
						const response = await store.authenticatedFetch(url);
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

						return contest;
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

						return contest;
					} finally {
						store.nbContestFetching--;
					}
				}
				return fetchFromUrl(
					"/api/contests?game_id=" + gameId + "&contest_id=" + contestId,
				);
			});
		},

		async loadContestIfMissing(gameId, contestId) {
			this.loadGameIfMissing(gameId);

			if (this.contests[contestId]) {
				console.log("Skip loading contestId=", contestId);
				return Promise.resolve(this.contests[contestId]);
			} else {
				return this.loadContest(gameId, contestId);
			}
		},

		async loadBoard(gameId, contestId, playerId) {
			console.log("gameId", gameId);
			if (!playerId) {
				playerId = this.playingPlayerId;
			}

			const store = this;

			return this.loadGameIfMissing(gameId)
				.then((game) => {
					return this.loadContestIfMissing(gameId, contestId);
				})
				.then((contest) => {
					async function fetchFromUrl(url) {
						store.nbBoardFetching++;
						try {
							const response = await store.authenticatedFetch(url);
							if (!response.ok) {
								throw new NetworkError(
									"Rejected request for contest: " + contestId,
									url,
									response,
								);
							}

							const responseJson = await response.json();

							const board = responseJson;

							// BEWARE This is broken if we consider a user can manage multiple playerIds
							console.log("Storing board for contestId", contestId);
							store.$patch({ boards: { ...store.boards, [contestId]: board } });

							return board;
						} catch (e) {
							console.error("Issue on Fetch: ", e, e.response.status);

							const board = { contestId: contestId, status: "error", error: e };
							store.$patch({ boards: { ...store.boards, [contestId]: board } });

							return board;
						} finally {
							store.nbBoardFetching--;
						}
					}

					return fetchFromUrl(
						"/api/board?game_id=" +
							gameId +
							"&contest_id=" +
							contestId +
							"&player_id=" +
							playerId,
					);
				});
		},

		async loadLeaderboard(gameId, contestId) {
			this.loadGameIfMissing(gameId);
			this.loadContestIfMissing(gameId, contestId);

			const store = this;

			async function fetchFromUrl(url) {
				store.nbLeaderboardFetching++;
				try {
					const response = await store.authenticatedFetch(url);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected request for leaderboard: " + contestId,
							url,
							response,
						);
					}

					const responseJson = await response.json();

					const leaderboard = responseJson;

					// https://github.com/vuejs/pinia/discussions/440
					console.log("Storing leaderboard for contestId", contestId);

					store.$patch({
						leaderboards: { ...store.leaderboards, [contestId]: leaderboard },
					});
				} catch (e) {
					console.error("Issue on Fetch: ", e, e.response.status);

					const leaderboard = {
						contestId: contestId,
						status: "error",
						error: e,
					};
					store.$patch({
						leaderboards: { ...store.leaderboards, [contestId]: leaderboard },
					});
					return leaderboard;
				} finally {
					store.nbLeaderboardFetching--;
				}
			}
			return fetchFromUrl("/api/leaderboards?contest_id=" + contestId);
		},
	},
});
