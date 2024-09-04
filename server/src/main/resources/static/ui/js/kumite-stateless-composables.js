// my-component.js
import { ref } from "vue";
import KumiteLeaderboard from "./kumite-leaderboard.js";
import KumiteBoardMoveForm from "./kumite-board-move-form.js";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteLeaderboard,
		KumiteBoardMoveForm,
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

		return {};
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
	
	<KumiteBoardMoveForm class="border" :gameId="gameId" :contestId="contestId" />
	
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
