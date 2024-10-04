import { onMounted } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

export default {
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
		...mapState(useUserStore, ["playingPlayerId"]),
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();
		const userStore = useUserStore();

		store.loadContestIfMissing(props.contestId, props.gameId);

		// https://fr.javascript.info/websocket
		onMounted(() => {
			// https://stackoverflow.com/questions/10406930/how-to-construct-a-websocket-uri-relative-to-the-page-uri
			const url = window.location;
			let wsUri;
			if (url.protocol === "https:") {
				wsUri = "wss:";
			} else {
				wsUri = "ws:";
			}
			wsUri += "//" + url.host;
			let socket = new WebSocket(wsUri + "/ws/contests");

			socket.onopen = function (e) {
				console.log("[open] Connection established");
				console.log("Sending to server");
				socket.send(JSON.stringify({ playerId: userStore.playingPlayerId }));
			};

			socket.onmessage = function (event) {
				console.log(`[message] Data received from server: ${event.data}`);
			};

			socket.onclose = function (event) {
				if (event.wasClean) {
					console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
				} else {
					// par exemple : processus serveur arrêté ou réseau en panne
					// event.code est généralement 1006 dans ce cas
					console.log("[close] Connection died");
				}
			};

			socket.onerror = function (error) {
				console.log(`[error]`, error);
			};
		});

		return {};
	},
	template: /* HTML */ ` <span /> `,
};
