// my-component.js
import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
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
<div v-else-if="game.error || contest.error">
	{{game.error || contest.error}}
</div>
<span v-else>
	<h2>
		<RouterLink :to="{path:'/html/games/' + gameId + '/contest/' + contestId}"><i class="bi bi-trophy"></i> {{contest.name}}</RouterLink>
		<RouterLink :to="{path:'/html/games/' + gameId}"><i class="bi bi-arrow-return-left"></i></RouterLink>
	</h2>
</span>
  `,
};
