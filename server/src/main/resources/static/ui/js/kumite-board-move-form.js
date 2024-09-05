import { ref } from "vue";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteBoardMoveJson from "./kumite-board-move-json.js";
import KumiteBoardMoveTSP from "./kumite-board-move-tsp.js";

// Duplicated from store.js
// TODO How can we share such a class?
class NetworkError extends Error {
	constructor(message, url, response) {
		super(message);
		this.name = this.constructor.name;

		this.url = url;
		this.response = response;
	}
}

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteBoardMoveJson,
		KumiteBoardMoveTSP,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		contestId: {
			type: String,
			required: true,
		},
		gameId: {
			type: String,
			required: true,
		},
		showCurl: {
			type: Boolean,
			default: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, [
			"nbGameFetching",
			"nbContestFetching",
			"nbBoardFetching",
			"playingPlayerId",
		]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
			board(store) {
				return store.boards[this.contestId];
			},
		}),
		curlPostBoard() {
			return (
				"curl " +
				window.location.protocol +
				"//" +
				window.location.host +
				"/api/board?contest_id=" +
				this.contestId
			);
		},
	},
	methods: {
		async loadExampleMoves() {
			const exampleMoves = this.exampleMoves;
			const exampleMovesMetadata = this.exampleMovesMetadata;

			const store = useKumiteStore();

			console.log("Loading example moves");
			async function fetchFromUrl(url) {
				try {
					const response = await store.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for games url" + url);
					}

					const responseJson = await response.json();
					const newExampleMoves = responseJson.moves;

					// This convoluted `modify` is needed until we clarify how wo can edit the Ref from this method
					// https://stackoverflow.com/questions/26957719/replace-object-value-without-replacing-reference
					function modify(obj, newObj) {
						Object.keys(obj).forEach(function (key) {
							delete obj[key];
						});

						Object.keys(newObj).forEach(function (key) {
							obj[key] = newObj[key];
						});
					}

					exampleMovesMetadata.loaded = true;

					// https://stackoverflow.com/questions/61452458/ref-vs-reactive-in-vue-3
					modify(exampleMoves, newExampleMoves);
				} catch (e) {
					console.error("Issue on Network: ", e);
					exampleMovesMetadata.error = e;
				}
			}

			const viewingPlayerId = "00000000-0000-0000-0000-000000000000";
			const playerId = viewingPlayerId;
			fetchFromUrl(
				"/api/board/moves?contest_id=" +
					this.contestId +
					"&player_id=" +
					playerId,
			);
		},
		fillMove(json) {
			this.move = JSON.stringify(json);
		},
		sendMove() {
			const exampleMovesMetadata = this.exampleMovesMetadata;

			const rawMove = this.move;

			let move;
			try {
				move = JSON.parse(rawMove);
			} catch (e) {
				console.error("Issue parsing json: ", e);
				exampleMovesMetadata.error = e;
				return;
			}

			console.log("Submitting move", move);

			const store = useKumiteStore();

			async function postFromUrl(url) {
				try {
					const fetchOptions = {
						method: "POST",
						headers: { "Content-Type": "application/json" },
						body: JSON.stringify(move),
					};
					const response = await store.authenticatedFetch(url, fetchOptions);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected POST for move for games url=" + url,
							url,
							response,
						);
					}
				} catch (e) {
					console.error("Issue on Network:", e);
					exampleMovesMetadata.error = e;
				}
			}

			const playerId = this.playingPlayerId;
			postFromUrl(
				"/api/board/move?contest_id=" +
					this.contestId +
					"&player_id=" +
					playerId,
			);
		},
	},
	setup(props) {
		const store = useKumiteStore();

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		store.loadBoard(props.gameId, props.contestId);

		const exampleMoves = ref({});
		const exampleMovesMetadata = ref({ loaded: false });

		// We need to suggest a move is defined through JSON format
		const move = ref("{}");

		return { exampleMoves, exampleMovesMetadata, move };
	},
	template: `
<div v-if="(!game || !contest || !board) && (nbGameFetching > 0 || nbContestFetching > 0 || nbBoardFetching > 0)">
	<div class="spinner-border" role="status">
	  <span class="visually-hidden">Loading contestId={{contestId}}</span>
	</div>
</div>
<div v-else-if="game.error || contest.error || board.error">
	{{game.error || contest.error || board.error}}
</div>
<div v-else>
	<div class="border">
		Submit a move/solution:
		<form>
		  <div>
		  	<button type="button" @click="loadExampleMoves" class="btn btn-outline-secondary" v-if="!exampleMovesMetadata.loaded">Load some available moves</button>
			<div v-else>
				<div class="btn-group" v-if="Object.keys(exampleMoves).length > 0">
				  <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
				    Fill with an example move
				  </button>
				  <ul class="dropdown-menu">
				    <li><a class="dropdown-item" @click="fillMove(moveJson)" v-for="(moveJson, moveKey) in exampleMoves">{{moveKey}}</a></li>
				  </ul>
				</div>
			</div>
		  </div>
			<div class="mb-3">
			  <label for="move" class="form-label">Solution as JSON</label>
			  <textarea class="form-control" rows="3" v-model="move"></textarea>
			</div>
			<div>
				<KumiteBoardMoveJson :board="board" :move="move" />
				<KumiteBoardMoveTSP :board="board" :move="move" />
			</div>
			<div>
		  		<button type="button" @click="sendMove()"  class="btn btn-outline-primary">Submit</button>

		  		<span v-if="exampleMovesMetadata.error" class="alert alert-warning" role="alert">{{exampleMovesMetadata.error}}</span>
		  </div>
		</form>
	</div>
</div>
  `,
};
