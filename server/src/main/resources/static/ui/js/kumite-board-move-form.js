import { ref } from "vue";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteJsonBoardMove from "./board-renderers/kumite-json-board-move.js";
import KumiteTSPBoardMove from "./board-renderers/kumite-tsp-board-move.js";
import KumiteTicTacToeBoardMove from "./board-renderers/kumite-tictactoe-board-move.js";

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
		KumiteJsonBoardMove,
		KumiteTSPBoardMove,
		KumiteTicTacToeBoardMove,
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
				return store.contests[this.contestId]?.board || { error: "not_loaded" };
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
			console.debug("Loading example moves");
			async function fetchFromUrl(url) {
				try {
					const response = await store.authenticatedFetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for games url" + url);
					}

					const responseJson = await response.json();
					const newExampleMoves = responseJson.moves;

					console.info("Loaded example moves", responseJson);

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
				`/api/board/moves?contest_id=${props.contestId}&player_id=${playerId}`,
			);
		}
		function fillMove(json) {
			this.rawMove = JSON.stringify(json);
		}

		const sendMoveError = ref("");
		function sendMove() {
			let move;
			try {
				move = JSON.parse(this.rawMove);
			} catch (e) {
				console.error("Issue parsing json: ", e);
				sendMoveError.value = e.message;
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
							"POST has failed (" +
								response.statusText +
								" - " +
								response.status +
								")",
							url,
							response,
						);
					}

					// debugger;
					// context.emit('move-sent', {gameId: props.gameId, contestId: props.contestId});
					// The submitted move may have impacted the leaderboard
					store.$patch((state) => {
						if (!state.leaderboards[contestId]) {
							state.leaderboards[contestId] = {};
						}
						state.leaderboards[contestId].stale = true;
						state.contests[contestId].stale = true;
					});
					sendMoveError.value = "";
				} catch (e) {
					console.error("Issue on Network:", e);
					sendMoveError.value = e.message;
				}
			}

			const playerId = store.playingPlayerId;
			return postFromUrl(
				`/api/board/move?contest_id=${contestId}&player_id=${playerId}`,
			);
		}

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		store.loadBoard(props.gameId, props.contestId);

		const exampleMoves = ref({});
		const exampleMovesMetadata = ref({ loaded: false });

		// We need to suggest a move is defined through JSON format
		const rawMove = ref(JSON.stringify({ some: "json" }));

		store.loadBoard(props.gameId, props.contestId);

		return {
			sendMoveError,

			exampleMoves,
			exampleMovesMetadata,
			loadExampleMoves,
			rawMove,
			fillMove,
			sendMove,
		};
	},
	template: `
	<div v-if="(!game || !contest || !board)">
  <div v-if="(nbGameFetching > 0 || nbContestFetching > 0 || nbBoardFetching > 0)">
	   <div class="spinner-border" role="status">
	      <span class="visually-hidden">Loading contestId={{contestId}}</span>
	   </div>
       </div>

       <div v-else-if="game.error || contest.error || board.error">
          {{game.error || contest.error || board.error}}
       </div>
       <div v-else>
          ???
       </div>
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
			<span>
				<component :is="board.moveSvg" v-bind="{ 'board': board, 'rawMove': rawMove}" :rawMove="rawMove" class="text-center" />
			</span>
         </div>
			<!-- Move Submitter-->
         <div>
            <button type="button" @click="sendMove()"  class="btn btn-outline-primary">Submit</button>
            <span v-if="sendMoveError" class="alert alert-warning" role="alert">{{sendMoveError}}</span>
         </div>
      </form>
	</div>
  `,
};
