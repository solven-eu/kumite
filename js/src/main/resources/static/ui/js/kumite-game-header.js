import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteGameRef from "./kumite-game-ref.js"

export default {
    components: {
        KumiteGameRef,  
    },
	props: {
		gameId: {
			type: String,
			required: true,
		},
		withDescription: {
			type: Boolean,
			default: true,
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
		const store = useKumiteStore();

		store.loadGameIfMissing(props.gameId);

		return {};
	},
	template: /* HTML */ `
        <div v-if="(!game) && (nbGameFetching > 0)">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading gameId={{gameId}}</span>
            </div>
        </div>
        <div v-else-if="game.error">game.error={{game.error}}</div>
        <div v-else>
            <span>
                <span v-if="withDescription">
                    <h1>
                        <KumiteGameRef :gameId="game.gameId" />
                        <!--RouterLink :to="{path:'/html/games'}"><i class="bi bi-arrow-90deg-left"></i></RouterLink-->
                    </h1>
                    Game-Description: {{game.shortDescription}}
                </span>
                <span v-else>
                    <h5>
                        <KumiteGameRef :gameId="gameId" />
                    </h5>
                </span>
            </span>
        </div>
    `,
};
