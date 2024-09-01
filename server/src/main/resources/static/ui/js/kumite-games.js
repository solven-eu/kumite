// my-component.js
import { ref } from "vue";
import KumiteGame from "./kumite-game.js";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteGame,
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching"]),
		...mapState(useKumiteStore, {
			games(store) {
				return Object.values(store.games);
			},
		}),
	},
	setup() {
		const store = useKumiteStore();

		store.loadGames();

		return {};
	},
	template: `
  <div v-if="Object.keys(games) == 0 && nbGameFetching > 0">
  	Loading games
	</div>
	<div v-else class="container">
		<div class="row border" v-for="game in games">
		  	<KumiteGame :gameId="game.gameId"/>
	  </div>
  </div>
  `,
};
