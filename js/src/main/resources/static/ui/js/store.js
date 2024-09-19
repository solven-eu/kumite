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

class UserNeedsToLoginError extends Error {
	constructor(message) {
		super(message);
		this.name = this.constructor.name;
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
		// Initially, we assume we are logged-in as we may have a session cookie
		// May be turned to true by 401 on `loadUser()`
		needsToLogin: false,

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
		// Typically useful when an error is wrapped in the store
		onSwallowedError(error) {
			if (error instanceof NetworkError) {
				console.warn("An NetworkError is not being rethrown", error, error.response.status);
			} else {
				console.error("An Error is not being rethrown", error);
			}
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

			return fetchFromUrl("/api/public/v1/metadata");
		},

		// This would not fail if the User needs to login.
		// Callers would generally rely on `ensureUser()`
		async loadUser() {
			const store = this;

			async function fetchFromUrl(url) {
				let response;

				// The following block can fail if there is no netwrk connection
				// (Are we sure? Where are the unitTests?)
				{
					store.nbAccountLoading++;
					try {
						// Rely on session for authentication
						response = await fetch(url);
					} finally {
						store.nbAccountLoading--;
					}
				}

				if (response.status === 401) {
					throw new UserNeedsToLoginError("User needs to login");
				} else if (!response.ok) {
					// What is this scenario? ServerInternalError?
					throw new NetworkError("Rejected request for games url" + url, url, response);
				}

				// We can typically get a Network error while fetching the json
				const responseJson = await response.json();
				const user = responseJson;

				console.log("User is logged-in", user);
				store.needsToLogin = false;

				return user;
			}

			return fetchFromUrl("/api/login/v1/user")
				.then((user) => {
					store.$patch({ account: user });

					return user;
				})
				.catch((e) => {
					// Whatever the error, we tell the user needs to login
					console.warn("User needs to login");
					store.needsToLogin = true;

					const user = { error: e };
					return user;
				});
		},

		// @throws UserNeedsToLoginError if not logged-in
		async ensureUser() {
			if (Object.keys(this.account?.user || {}).length !== 0) {
				// We have loaded a user: we assume it does not need to login
				return Promise.resolve(this.account.user);
			} else {
				// We need first to load current user
				// It will enbale checking we are actually logged-in
				return this.loadUser().then((user) => {
					if (this.needsToLogin) {
						// We are still not logged-in
						throw new Error("The user needs to login");
					} else if (user.error) {
						throw new Error("Issue when loading the user: " + user.error);
					}

					return user;
				});
			}
		},

		async loadUserTokens() {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					// Rely on session for authentication
					const response = await fetch(url);
					if (!response.ok) {
						throw new NetworkError("Rejected request for games url" + url, url, response);
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
						(access_token_expired) => {
							if (access_token_expired) {
								console.log("access_token is expired. Triggering loadUserTokens");
								store.loadUserTokens();
							}
						},
					);

					return tokens;
				} catch (e) {
					store.onSwallowedError(e);
					return { error: e };
				} finally {
					store.nbAccountLoading--;
				}
			}

			return this.ensureUser().then(() => {
				console.log("We do have a User. Let's fetch tokens");
				return fetchFromUrl(`/api/login/v1/token?player_id=${this.playingPlayerId}`);
			});
		},

		async loadIfMissingUserTokens() {
			if (this.tokens.access_token && !this.tokens.access_token_expired) {
				console.debug("Authenticated and an access_tokenTokens is stored", this.tokens.access_token);
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
			const mergeHeaders = Object.assign({}, apiHeaders, fetchOptions.headers || {});

			const mergedFetchOptions = Object.assign({ method: "GET" }, fetchOptions);
			mergedFetchOptions.headers = mergeHeaders;

			console.debug("->", mergedFetchOptions.method, url, mergedFetchOptions);

			return fetch(url, mergedFetchOptions).then((response) => {
				console.debug("<-", mergedFetchOptions.method, url, mergedFetchOptions, response);

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
						throw new Error("Rejected request for current account players" + url);
					}

					const responseJson = await response.json();
					const players = responseJson;

					players.forEach((player) => {
						console.log("Registering playerId", player.playerId);
						store.$patch({
							players: {
								...store.players,
								[player.playerId]: player,
							},
						});
					});
				} catch (e) {
					store.onSwallowedError(e);
				} finally {
					store.nbAccountLoading--;
				}
			}

			return fetchFromUrl(`/api/players?account_id=${store.account.accountId}`);
		},
		async loadContestPlayers(contestId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await store.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for players of contest=" + contestId);
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
						contests: {
							...store.contests,
							[contestId]: mutatedContest,
						},
					});
				} catch (e) {
					store.onSwallowedError(e);
				} finally {
					store.nbAccountLoading--;
				}
			}

			return fetchFromUrl(`/api/players?contest_id=${contestId}`);
		},

		async loadGames() {
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

			return fetchFromUrl("/api/games");
		},
		async loadGameIfMissing(gameId) {
			if (this.games[gameId]) {
				console.debug("Skip loading gameId=", gameId);

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
						store.$patch({
							games: { ...store.games, [gameId]: game },
						});

						return game;
					} catch (e) {
						store.onSwallowedError(e);

						const game = {
							gameId: gameId,
							status: "error",
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
				return fetchFromUrl("/api/games?game_id=" + gameId);
			}
		},

		async loadContests(gameId) {
			const store = this;
			async function fetchFromUrl(url) {
				store.nbContestFetching++;
				try {
					const response = await store.authenticatedFetch(url);
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

			let url = "/api/contests";
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

				async function fetchFromUrl(url) {
					store.nbContestFetching++;
					try {
						const response = await store.authenticatedFetch(url);
						if (!response.ok) {
							throw new NetworkError("Rejected request for contest: " + contestId, url, response);
						}

						const responseJson = await response.json();

						if (responseJson.length === 0) {
							return { contestId: contestId, status: "unknown" };
						} else if (responseJson.length !== 1) {
							// This should not happen as we provided an input contestId
							console.error("We expected a single contest", responseJson);
							return { contestId: contestId, status: "unknown" };
						}

						const contest = responseJson[0];

						return contest;
					} catch (e) {
						store.onSwallowedError(e);

						const contest = {
							contestId: contestId,
							status: "error",
							error: e,
						};

						return contest;
					} finally {
						store.nbContestFetching--;
					}
				}
				return fetchFromUrl(`/api/contests?game_id=${gameId}&contest_id=${contestId}`).then((contest) => {
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
				playerId = this.playingPlayerId;
			}

			const store = this;

			return this.loadContestIfMissing(gameId, contestId).then((contest) => {
				if (contest.status === "unknown") {
					return contest;
				}

				async function fetchFromUrl(url) {
					store.nbBoardFetching++;
					try {
						const response = await store.authenticatedFetch(url);
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
							status: "error",
							error: e,
						};
					} finally {
						store.nbBoardFetching--;
					}
				}

				return fetchFromUrl(`/api/board?game_id=${gameId}&contest_id=${contestId}&player_id=${playerId}`).then((contestWithBoard) =>
					this.mergeContest(contestWithBoard),
				);
			});
		},

		async loadLeaderboard(gameId, contestId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbLeaderboardFetching++;
				try {
					const response = await store.authenticatedFetch(url);
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
						status: "error",
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

			return this.loadContestIfMissing(gameId, contestId).then(() => fetchFromUrl("/api/leaderboards?contest_id=" + contestId));
		},
	},
});