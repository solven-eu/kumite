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
				return store.games[this.gameId] || { error: "not_loaded" };
			},
			contest(store) {
				return store.contests[this.contestId] || { error: "not_loaded" };
			},
			board(store) {
				return store.boards[this.contestId]?.board || { error: "not_loaded" };
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
	setup(props) {
		const store = useKumiteStore();

		function loadExampleMoves() {
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

					exampleMovesMetadata.value.loaded = true;

					// https://stackoverflow.com/questions/61452458/ref-vs-reactive-in-vue-3
					modify(exampleMoves, newExampleMoves);
				} catch (e) {
					console.error("Issue on Network: ", e);
					exampleMovesMetadata.value.error = e;
				}
			}

			// const viewingPlayerId = "00000000-0000-0000-0000-000000000000";
			// const playerId = viewingPlayerId;
			const playerId = store.playingPlayerId;
			fetchFromUrl(
				"/api/board/moves?contest_id=" +
					props.contestId +
					"&player_id=" +
					playerId,
			);
		}
		function fillMove(json) {
			this.rawMove = JSON.stringify(json);
		}
		function sendMove() {
			let move;
			try {
				move = JSON.parse(this.rawMove);
			} catch (e) {
				console.error("Issue parsing json: ", e);
				return;
			}

			console.log("Submitting move", move);

			const contestId = this.contestId;

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
					// The submitted move may have impacted the leaderboard
					if (!store.leaderboards[contestId]) {
						store.leaderboards[contestId] = {};
					}
					store.leaderboards[contestId].stale = true;
				} catch (e) {
					console.error("Issue on Network:", e);
				}
			}

			const playerId = store.playingPlayerId;
			postFromUrl(
				"/api/board/move?contest_id=" + contestId + "&player_id=" + playerId,
			);
		}

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		store.loadBoard(props.gameId, props.contestId);

		const exampleMoves = ref({});
		const exampleMovesMetadata = ref({ loaded: false });

		// We need to suggest a move is defined through JSON format
		const rawMove = ref("{}");

		const showBoardWithMoveAsSvg = ref(false);
		store.loadBoard(props.gameId, props.contestId).then((board) => {
			// If this board enables SVG, activates it by default
			showBoardWithMoveAsSvg.value = store.boards[props.contestId].svg;
		});

		return {
			exampleMoves,
			exampleMovesMetadata,
			loadExampleMoves,
			rawMove,
			fillMove,
			sendMove,
			showBoardWithMoveAsSvg,
		};
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
	<div v-else class="container border">
      <form>
	  	<!-- Move Editor -->
         <div class="row">
			<div class="col">
			   <label for="move" class="form-label">Submit a move/solution:</label>
			   <textarea class="form-control" rows="3" v-model="rawMove"></textarea>
			</div>
		 	<div class="col my-auto">
			 	<span v-if="!exampleMovesMetadata.loaded">
	            	<button type="button" @click="loadExampleMoves" class="btn btn-outline-secondary">Load some available moves</button>
				</span>
					<span v-else-if="Object.keys(exampleMoves).length > 0" class="btn-group ">
					   <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
					   	Prefill with an example move
					   </button>
					   <ul class="dropdown-menu">
					      <li><a class="dropdown-item" @click="fillMove(moveJson)" v-for="(moveJson, moveKey) in exampleMoves">{{moveKey}}</a></li>
					   </ul>
					</span>
					   <span v-else>
					      Not a single example move. It may not be your turn.
					   </span>
		   </div>
         </div>
		 <!-- Move Visualizer-->
         <div class="row">
			<div class="form-check form-switch">
			   <input class="form-check-input" type="checkbox" role="switch" id="flexSwitchCheckDefault" v-model="showBoardWithMoveAsSvg">
			   <label class="form-check-label" for="flexSwitchCheckDefault">Show as SVG</label>
			</div>
			<span>
				<KumiteBoardMoveTSP :board="board" :rawMove="rawMove" v-if="showBoardWithMoveAsSvg" class="text-center" />
				<KumiteBoardMoveJson :board="board" :rawMove="rawMove" v-else class="text-center" />
			</span>
         </div>
			<!-- Move Submitter-->
         <div>
            <button type="button" @click="sendMove()"  class="btn btn-outline-primary">Submit</button>
            <span v-if="exampleMovesMetadata.error" class="alert alert-warning" role="alert">{{exampleMovesMetadata.error}}</span>
         </div>
      </form>
	</div>
  `,
};
