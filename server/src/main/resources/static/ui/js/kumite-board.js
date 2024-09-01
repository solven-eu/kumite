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
		showGame: {
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
	},
	setup(props) {
		const store = useKumiteStore();

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
	{{game.error || contest.error}}
</div>
<div v-else>
	<span v-if="showGame">
		<h1>Game: {{game.title}}</h1>
		Game-Description: {{game.shortDescription}}<br/>
	</span>
	<h2>{{contest.name}}</h2>
	
	Board:
	<pre  style="height: 10pc; overflow-y: scroll;" class="border">
{{board}}
	</pre>
	
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
			<div class="mb-3">
			  <label for="solution" class="form-label">Solution as JSON</label>
			  <textarea class="form-control" id="solution" rows="3"></textarea>
			</div>
		  <button type="submit" class="btn btn-primary">Submit</button>
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
