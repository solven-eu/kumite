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

const prefix = "/api/v1";

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
		account: { raw: {} },
		tokens: {},
		// Some very first check to know if we are potentially logged-in
		// (May check some Cookie or localStorage, or some API preferably returning 2XX even if logged-in)
		needsToCheckLogin: true,

		// We loads information about various players (e.g. current account, through contests and leaderboards)
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
		// If true, we have an account details. Hence we can logout.
		// If false, we need to check `needsToCheckLogin`
		isLoggedIn: (store) => Object.keys(store.account.raw).length > 0,
		isLoggedOut: (store) => {
			if (store.isLoggedIn) {
				// No need to login as we have an account (hence presumably relevant Cookies/tokens)
				return false;
			} else if (store.needsToCheckLogin) {
				// We need to check login: we are not clearly logged-out
				return false;
			}

			// Not logged-in and login-status is checked explicitly
			return true;
		},
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

			return fetchFromUrl(prefix + "/public/metadata");
		},

		async fetchCsrfToken() {
			// https://www.baeldung.com/spring-security-csrf
			// If we relied on Cookie, `.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())` we could get the csrfToken with:
			// const csrfToken = document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');

			const response = await fetch(`/api/login/v1/csrf`);
			if (!response.ok) {
				throw new Error("Rejected request for logout");
			}

			const json = await response.json();
			const csrfHeader = json.header;
			console.log("csrf header", csrfHeader);

			const freshCrsfToken = response.headers.get(csrfHeader);
			if (!freshCrsfToken) {
				throw new Error("Invalid csrfToken");
			}
			console.debug("csrf", freshCrsfToken);

			return { header: csrfHeader, token: freshCrsfToken };
		},

		// The point of this method is to detect login, without any 401 call, hence without any error or exception
		async fetchLoginStatus() {
			const response = await fetch(`/api/login/v1/json`);
			if (!response.ok) {
				throw new Error("Rejected request for login.json");
			}

			const json = await response.json();

			const loginHttpStatus = json.login;
			console.log("login", loginHttpStatus);

			return loginHttpStatus;
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

				return user;
			}

			return store.fetchLoginStatus().then((loginHttpStatus) => {
				store.needsToCheckLogin = false;

				if (loginHttpStatus === 200) {
					// We are logged-in

					return fetchFromUrl("/api/login/v1/user")
						.then((user) => {
							store.$patch({ account: user });

							return user;
						})
						.catch((e) => {
							// Issue loadingUser while we checked the browser is logged-in
							console.warn("User needs to login");

							const user = { error: e };
							store.$patch({ account: user });
							return user;
						});
				} else {
					// Typically happens on first visit
					console.info("User needs to login");

					// We are not logged-in
					const e = new UserNeedsToLoginError("User needs to login");
					const user = { error: e };
					return user;
				}
			});
		},

		// @throws UserNeedsToLoginError if not logged-in
		async ensureUser() {
			if (this.isLoggedIn) {
				// We have loaded a user: we assume it does not need to login
				return Promise.resolve(this.account);
			} else if (this.isLoggedOut) {
				// We are not logged-in
				throw new UserNeedsToLoginError("User needs to login");
			} else {
				// We need first to load current user
				// It will enbale checking we are actually logged-in
				return this.loadUser().then((user) => {
					if (this.isLoggedIn) {
						return user;
					} else {
						// We are still not logged-in (e.g. session expired)
						throw new Error("The user needs to login");
					}
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
						throw new NetworkError("Rejected request for tokens", url, response);
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

			return this.loadUser().then((user) => {
				if (store.isLoggedIn) {
					console.log("We do have a User. Let's fetch tokens", user);
					return fetchFromUrl(`/api/login/v1/oauth2/token?player_id=${this.playingPlayerId}`);
				} else {
					return { error: "not_logged_in" };
				}
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
			if (url.startsWith("/api")) {
				throw new Error("Invalid URL as '/api' is added automatically");
			}

            // loading missing tokens will ensure login status
			await this.loadIfMissingUserTokens();
            
            if (this.isLoggedOut) {
                throw new UserNeedsToLoginError("User needs to login");
            }

			const apiHeaders = this.apiHeaders;

			// fetchOptions are optional
			fetchOptions = fetchOptions || {};

			// https://stackoverflow.com/questions/171251/how-can-i-merge-properties-of-two-javascript-objects
			const mergeHeaders = Object.assign({}, apiHeaders, fetchOptions.headers || {});

			const mergedFetchOptions = Object.assign({ method: "GET" }, fetchOptions);
			mergedFetchOptions.headers = mergeHeaders;

			console.debug("->", mergedFetchOptions.method, url, mergedFetchOptions);

			return fetch(prefix + url, mergedFetchOptions).then((response) => {
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

			return fetchFromUrl(`/players?account_id=${store.account.accountId}`);
		},

		async loadPlayer(playerId) {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbAccountLoading++;
				try {
					const response = await store.authenticatedFetch(url);
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
					store.nbAccountLoading--;
				}
			}

			return fetchFromUrl(`/players?player_id=${playerId}`);
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
					store.nbAccountLoading--;
				}
			}

			return fetchFromUrl(`/players?contest_id=${contestId}`);
		},

		async loadGames() {
			const store = this;

			async function fetchFromUrl(url) {
				store.nbGameFetching++;

				try {
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

			return fetchFromUrl("/games");
		},
		async loadGameIfMissing(gameId) {
			if (this.games[gameId]) {
				console.debug("Already stored gameId=", gameId);

				return Promise.resolve(this.games[gameId]);
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

				async function fetchFromUrl(url) {
					store.nbContestFetching++;
					try {
						const response = await store.authenticatedFetch(url);
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
				playerId = this.playingPlayerId;
			}

			const store = this;

			return this.loadContestIfMissing(gameId, contestId).then((contest) => {
				if (contest.error === "unknown") {
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
