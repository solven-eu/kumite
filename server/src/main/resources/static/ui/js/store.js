import { defineStore } from "pinia";

export const useKumiteStore = defineStore("kumite", {
	state: () => ({ games: {}, contests: {} }),
	getters: {
		doubleCount: (state) => state.count * 2,
	},
	actions: {
		increment() {
			this.count++;
		},
		async loadGames(gameId) {
			const store = this;
			async function theData(url) {
				try {
					// isLoading.value = true;
					const response = await fetch(url);
					const responseJson = await response.json();

					responseJson.forEach((item) => {
						console.log("Registering gameId", item.gameId);
						store.$patch({ games: { ...store.games, [item.gameId]: item } });
					});

					// store.games.value = responseJson;
				} catch (e) {
					console.error("Issue on Network: ", e);
					// error.value = e;
				} finally {
					// isLoading.value = false;
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
					try {
						// isLoading.value = true;
						const response = await fetch(url);
						const responseJson = await response.json();

						if (responseJson.length !== 1) {
							console.error("We expected a single entry", responseJson);
						}

						const game = responseJson[0];
						// gameRef.value =

						// https://github.com/vuejs/pinia/discussions/440
						//const gameId = gameId;
						console.log("Registering gameId", gameId);
						store.$patch({ games: { ...store.games, [gameId]: game } });
					} catch (e) {
						console.error("Issue on Network: ", e);
						// error.value = e;
					} finally {
						// isLoading.value = false;
					}
				}
				theData("/api/games?game_id=" + gameId);
			}
		},
	},
});
