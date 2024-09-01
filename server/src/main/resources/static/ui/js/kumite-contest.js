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

		store.loadContestIfMissing(props.gameId, props.contestId);

		return {};
	},
	template: `
<div v-if="(!game || !contest) && (nbGameFetching > 0 || nbContestFetching > 0)">
	<div class="spinner-border" role="status">
	  <span class="visually-hidden">Loading contestId={{contestId}}</span>
	</div>
</div>
<div v-else>
	<span v-if="showGame">
		<h1>Game: {{game.title}}</h1>
		Game-Description: {{game.shortDescription}}<br/>
	</span>
	<h2>{{contest.name}}</h2>
	
	<div v-if="contest.acceptPlayers">
		<RouterLink :to="{path:'/games/' + gameId + '/contest/' + contestId}">
			<button type="button" class="btn btn-outline-primary">Join this contest ({{contest.nbActivePlayers}} players)</button>
		</RouterLink>
	</div>
	
	<KumiteLeaderboard :contestId="contestId"/>
</div>
  `,
};
