// my-component.js
import { ref } from "vue";
import KumiteLeaderboard from "./kumite-leaderboard.js";
import KumiteBoardMoveForm from "./kumite-board-move-form.js";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteBoardJson from "./kumite-board-state-json.js";
import KumiteBoardTSP from "./kumite-board-state-tsp.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteLeaderboard,
		KumiteBoardMoveForm,
		KumiteBoardJson,
		KumiteBoardTSP,
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

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		store.loadBoard(props.gameId, props.contestId);

		const showBoardAsSvg = ref(true);

		return { showBoardAsSvg };
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
	<div v-else class="container">
	   <span class="row">
	      <h1>Game: {{game.title}}</h1>
	      Game-Description: {{game.shortDescription}}<br/>
	   </span>
	   <span class="row">
	      <h2>Contest: {{contest.name}}</h2>
	   </span>
	   <div class="row border">
	      <div class="">
	         Board: This is the view of the contest given players previous moves.
	         <div class="form-check form-switch">
	            <input class="form-check-input" type="checkbox" role="switch" id="flexSwitchCheckDefault" v-model="showBoardAsSvg">
	            <label class="form-check-label" for="flexSwitchCheckDefault">Show as SVG</label>
	         </div>
	         <KumiteBoardTSP :board="board" v-if="showBoardAsSvg" class="col text-center" />
	         <span v-else>
	            <KumiteBoardJson :board="board" class="col" />
	            <div v-if="showCurl">
	               cURL command: 
	               <pre  style="overflow-y: scroll;" class="border">{{curlGetBoard}}</pre>
	            </div>
	         </span>
	      </div>
	   </div>
	   <div class="row border" v-if="contest">
	      <div v-if="contest.acceptPlayers">
	         This contest accepts players.
	         <KumiteBoardMoveForm :gameId="gameId" :contestId="contestId" />
	      </div>
	      <div v-else>
	         This contest is full.
	      </div>
	   </div>
	   <div class="row border">
	      <KumiteLeaderboard :gameId="gameId" :contestId="contestId"/>
	   </div>
	</div>
  `,
};
