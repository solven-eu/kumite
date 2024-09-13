import { watch } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteBoardViewCurl from "./kumite-board-view-curl.js";

import KumiteJsonBoardState from "./board-renderers/kumite-json-board-state.js";
import KumiteTSPBoardState from "./board-renderers/kumite-tsp-board-state.js";
import KumiteTicTacToeBoardState from "./board-renderers/kumite-tictactoe-board-state.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteBoardViewCurl,

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
				return store.contests[this.contestId]?.board;
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		watch(
			() => store.contests[props.contestId]?.stale,
			(stale) => {
				if (stale) {
					console.log("Detected stale board", props.contestId);
					store.loadBoard(props.gameId, props.contestId);
				}
			},
		);

		return {};
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
	   <div v-else >
       <div class="border position-relative">
	         Board: This is the view of the contest given players previous moves.
			 	<!-- https://stackoverflow.com/questions/43658481/passing-props-dynamically-to-dynamic-component-in-vuejs -->
				<component :is="board.boardSvg" v-bind="{ 'board': board}" class="col text-center" />
                
                <div  class="position-absolute top-0 end-0" >
                    <KumiteBoardViewCurl :gameId="gameId" :contestId="contestId" />
                </div>
                </div>
	   </div>
  `,
};
