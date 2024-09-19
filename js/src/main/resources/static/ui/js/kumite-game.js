import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
import { Tooltip } from "bootstrap";

import KumiteGameHeader from "./kumite-game-header.js";

import KumiteContests from "./kumite-contests.js";

export default {
	components: {
		KumiteGameHeader,
		KumiteContests,
	},
	props: {
		gameId: {
			type: String,
			required: true,
		},
		showContests: {
			type: Boolean,
			default: false,
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

		//store.loadMetadata();
		// const gameRef = ref(store.game);

		// console.log("gameId", props.gameId);
		// console.log("game", props.game);

		const nbContests = ref("...");

		store.loadGameIfMissing(props.gameId).then(() => {
			store.loadContests(props.gameId).then((contests) => {
				nbContests.value = contests.length;
			});
		});

		// https://getbootstrap.com/docs/5.3/components/tooltips/
		// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
		new Tooltip(document.body, { selector: "[data-bs-toggle='tooltip']" });

		return { nbContests };
	},
	template: /* HTML */ `
        <div v-if="!game && nbGameFetching > 0">Loading <RouterLink :to="{path:'/html/games/' + gameId}">game={{gameId}}</RouterLink></div>
        <div v-else-if="game.error">{{game.error}}</div>
        <div v-else>
            <KumiteGameHeader :gameId="gameId" />

            <span v-if="metadata.tags">
                Tags: <span class="badge text-bg-secondary" v-for="tag in game.tags" data-bs-toggle="tooltip" :data-bs-title="metadata.tags[tag]">{{tag}}</span
                ><br />
            </span>
            <ul v-for="ref in game.references">
                <li><a :href="ref" target="_blank">{{ref}}</a></li>
            </ul>

            <span v-if="showContests">
                <KumiteContests :gameId="gameId" :showGame="false" />
            </span>
            <span v-else>
                <RouterLink :to="{path:'/html/games/' + game.gameId + '/contests'}"
                    ><i class="bi bi-trophy"></i> Join an existing contest ({{nbContests}})</RouterLink
                ><br />
            </span>

            <RouterLink :to="{path:'/html/games/' + game.gameId + '/contest-form'}"><i class="bi bi-node-plus"></i> Create your own contest</RouterLink><br />
        </div>
    `,
};
