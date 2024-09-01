import { defineStore } from "pinia";

export const useKumiteStore = defineStore("kumite", {
	state: () => ({
		games: {},
		contests: {},
		nbGameFetching: 0,
		nbContestFetching: 0,
	}),
	getters: {
		doubleCount: (state) => state.count * 2,
	},
	actions: {
		async loadGames(gameId) {
			const store = this;

			async function theData(url) {
				store.nbGameFetching++;
				try {
					const response = await fetch(url);
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

			theData("/api/games");
		},
		async loadGameIfMissing(gameId) {
			if (this.games[gameId]) {
				console.log("Skip loading gameId=", gameId);
			} else {
				console.log("About to load gameId", gameId);

				const store = this;

				async function theData(url) {
					store.nbGameFetching++;
					try {
						const response = await fetch(url);
						const responseJson = await response.json();

						if (responseJson.length !== 1) {
							console.error("We expected a single game", responseJson);
						}

						const game = responseJson[0];

						// https://github.com/vuejs/pinia/discussions/440
						console.log("Registering gameId", gameId);
						store.$patch({ games: { ...store.games, [gameId]: game } });
					} catch (e) {
						console.error("Issue on Network: ", e);
					} finally {
						store.nbGameFetching--;
					}
				}
				theData("/api/games?game_id=" + gameId);
			}
		},

		async loadContests(gameId) {
			const store = this;
			async function theData(url) {
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
				theData("/api/contests?game_id=" + gameId);
			} else {
				// Cross-through contests
				theData("/api/contests");
			}
		},

		async loadContestIfMissing(gameId, contestId) {
			this.loadGameIfMissing(gameId);

			if (this.contests[contestId]) {
				console.log("Skip loading contestId=", contestId);
			} else {
				console.log("About to load contestId", contestId);

				const store = this;

				async function theData(url) {
					store.nbContestFetching++;
					try {
						const response = await fetch(url);
						const responseJson = await response.json();

						if (responseJson.length !== 1) {
							console.error("We expected a single contest", responseJson);
						}

						const contest = responseJson[0];

						// https://github.com/vuejs/pinia/discussions/440
						console.log("Registering contestId", contestId);
						store.$patch({
							contests: { ...store.contests, [contestId]: contest },
						});
					} catch (e) {
						console.error("Issue on Network: ", e);
					} finally {
						store.nbContestFetching++;
					}
				}
				theData("/api/contests?game_id=" + gameId + "&contest_id=" + contestId);
			}
		},
	},
});
