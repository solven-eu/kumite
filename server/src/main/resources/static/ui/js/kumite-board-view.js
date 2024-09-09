import { ref, watch } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteJsonBoardState from "./board-renderers/kumite-json-board-state.js";
import KumiteTSPBoardState from "./board-renderers/kumite-tsp-board-state.js";
import KumiteTicTacToeBoardState from "./board-renderers/kumite-tictactoe-board-state.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteJsonBoardState,
		KumiteTSPBoardState,
		KumiteTicTacToeBoardState,
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
				return store.boards[this.contestId]?.board;
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
	setup(props) {
		const store = useKumiteStore();

		const showBoardAsJson = ref(false);

		watch(
			() => store.boards[props.contestId]?.stale,
			(newValue) => {
				console.log("Detected stale board", props.contestId);
				store.loadBoard(props.gameId, props.contestId);
			},
		);
		
		return { showBoardAsJson };
	},
	template: `
	<div v-if="(!game || !contest || !board)">
		<div class="spinner-border" role="status" v-if="(nbGameFetching > 0 || nbContestFetching > 0 || nbBoardFetching > 0)">
		   <span class="visually-hidden">Loading board for contestId={{contestId}}</span>
		</div>
	   <div class="spinner-border" role="status" v-else>
	      <span class="visually-hidden">Issue loading board for contestId={{contestId}}</span>
	   </div>
	</div>
	<div v-else-if="game.error || contest.error || board.error">
	   {{game.error || contest.error || board.error}}
	</div>
	   <div v-else class="border">
	         Board: This is the view of the contest given players previous moves.
	         <div class="form-check form-switch">
	            <input class="form-check-input" type="checkbox" role="switch" id="flexSwitchCheckDefault" v-model="showBoardAsJson">
	            <label class="form-check-label" for="flexSwitchCheckDefault">Show as SVG</label>
	         </div>
			 	<!-- https://stackoverflow.com/questions/43658481/passing-props-dynamically-to-dynamic-component-in-vuejs -->
				<component :is="board.boardSvg" v-bind="{ 'board': board}" class="col text-center" />
	            <div v-if="showCurl">
	               cURL command: 
	               <pre  style="overflow-y: scroll;" class="border">{{curlGetBoard}}</pre>
	            </div>
	   </div>
  `,
};
