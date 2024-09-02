// my-component.js
import { ref } from "vue";
import KumiteLeaderboard from "./kumite-leaderboard.js";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteLeaderboard,
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
		curlGetBoard() {
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

			console.log("Loading example moves");
			async function fetchFromUrl(url) {
				try {
					const response = await fetch(url);
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
					// https://stackoverflow.com/questions/61452458/ref-vs-reactive-in-vue-3
					modify(exampleMoves, newExampleMoves);
				} catch (e) {
					console.error("Issue on Network: ", e);
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
			document.getElementById("solution").innerHTML = JSON.stringify(json);
		},
	},
	setup(props) {
		const store = useKumiteStore();

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		store.loadBoard(props.gameId, props.contestId);

		const exampleMoves = ref({});

		return { exampleMoves };
	},
	// https://stackoverflow.com/questions/7717929/how-do-i-get-pre-style-overflow-scroll-height-150px-to-display-at-parti
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
	<span>
		<h1>Game: {{game.title}}</h1>
		Game-Description: {{game.shortDescription}}<br/>
	</span>
	<h2>{{contest.name}}</h2>

	<div class="border">
		Board:
		<pre  style="height: 10pc; overflow-y: scroll;" class="border">{{board}}</pre>
		
		<div v-if="showCurl">
			<pre  style="overflow-y: scroll;" class="border">{{curlGetBoard}}</pre>
		</div>
	</div>
	
	<div class="border">
		Submit a solution:
		<form>
		  <div class="mb-3">
		    <label for="player" class="form-label">Playing player</label>
			<select class="form-select" aria-label="Default select example" id="player" aria-describedby="playerHelp">
			  <option selected>AccountMainPlayer</option>
			  <!--option value="1">One</option-->
			</select>
			  <div id="playerHelp" class="form-text">We'll never share your email with anyone else.</div>
		  </div>
		  <div>
		  	<button  type="button" @click="loadExampleMoves" class="btn btn-outline-secondary">Load some available moves</button>
			<div class="btn-group" v-if="Object.keys(exampleMoves).length > 0">
			  <button type="button" class="btn btn-danger dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
			    Fill with an example move
			  </button>
			  <ul class="dropdown-menu">
			    <li><a class="dropdown-item" @click="fillMove(moveJson)" v-for="(moveJson, moveKey) in exampleMoves">{{moveKey}}</a></li>
			  </ul>
			</div>
		  </div>
			<div class="mb-3">
			  <label for="solution" class="form-label">Solution as JSON</label>
			  <textarea class="form-control" id="solution" rows="3"></textarea>
			</div>
		  <button type="submit" class="btn btn-outline-primary">Submit</button>
		</form>
	</div>
	
	<div v-if="contest.acceptPlayers">
		This contest accepts players.
	</div>
	<div v-else>
		This contest is full.
	</div
	
	<KumiteLeaderboard :contestId="contestId"/>
</div>
  `,
};
