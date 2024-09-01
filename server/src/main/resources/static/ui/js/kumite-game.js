// my-component.js
import { ref } from "vue";
import KumiteContests from "./kumite-contests.js";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteContests,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		gameId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
		}),
	},
	setup(props) {
		// const error = ref({});
		// const isLoading = ref(true);

		const store = useKumiteStore();
		// const gameRef = ref(store.game);

		// console.log("gameId", props.gameId);
		// console.log("game", props.game);

		store.loadGameIfMissing(props.gameId);

		return {};
	},
	template: `
<div v-if="!game && nbGameFetching > 0">
  	Loading <RouterLink :to="{path:'/games/' + gameId}">game={{gameId}}</RouterLink>
</div>
<div v-else-if="game.error">
	{{game.error}}
</div>
<div v-else>
	<h1><RouterLink :to="{path:'/games/' + game.gameId}">{{game.title}}</RouterLink></h1>
	
	Description: {{game.shortDescription}}<br/>
	<ul v-for="ref in game.references">
		<li><a :href="ref" target="_blank">{{ref}}</a></li>
	</ul>
	<KumiteContests :gameId="gameId"/>
</div>
  `,
};
