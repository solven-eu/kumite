// my-component.js
import { ref } from "vue";
import KumiteContests from "./kumite-contests.js";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
import { Tooltip } from "bootstrap";

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
		...mapState(useKumiteStore, ["nbGameFetching", "metadata"]),
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

		store.loadMetadata();
		// const gameRef = ref(store.game);

		// console.log("gameId", props.gameId);
		// console.log("game", props.game);

		store.loadGameIfMissing(props.gameId);

		// https://getbootstrap.com/docs/5.3/components/tooltips/
		const tooltipTriggerList = document.querySelectorAll(
			'[data-bs-toggle="tooltip"]',
		);
		const tooltipList = [...tooltipTriggerList].map(
			(tooltipTriggerEl) => new bootstrap.Tooltip(tooltipTriggerEl),
		);

		// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3

		new Tooltip(document.body, { selector: "[data-bs-toggle='tooltip']" });

		return {};
	},
	template: `
<div v-if="!game && nbGameFetching > 0">
  	Loading <RouterLink :to="{path:'/html/games/' + gameId}">game={{gameId}}</RouterLink>
</div>
<div v-else-if="game.error">
	{{game.error}}
</div>
<div v-else>
	<h1><RouterLink :to="{path:'/html/games/' + game.gameId}">{{game.title}}</RouterLink></h1>
	
	<span v-if="metadata.tags">
		Tags: <span class="badge text-bg-secondary" v-for="tag in game.tags" data-bs-toggle="tooltip" :data-bs-title="metadata.tags[tag]">{{tag}}</span><br/>
	</span>
	Description: {{game.shortDescription}}<br/>
	<ul v-for="ref in game.references">
		<li><a :href="ref" target="_blank">{{ref}}</a></li>
	</ul>
	<KumiteContests :gameId="gameId" :showGame="false"/>
</div>
  `,
};
