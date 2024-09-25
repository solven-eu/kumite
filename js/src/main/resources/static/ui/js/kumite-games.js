import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import LoginRef from "./login-ref.js";

import KumiteGame from "./kumite-game.js";

export default {
	components: {
		LoginRef,
		KumiteGame,
	},
	computed: {
		...mapState(useKumiteStore, ["isLoggedIn", "nbGameFetching"]),
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
	template: /* HTML */ `
        <div v-if="!isLoggedIn"><LoginRef /></div>
        <div v-if="Object.keys(games) == 0">
            <div v-if="nbGameFetching > 0">Loading games</div>
            <div v-else>Issue loading games (or no games at all)</div>
        </div>
        <div v-else class="container">
            <div class="row border" v-for="game in games">
                <KumiteGame :gameId="game.gameId" :showContests="false" v-if="!game.error" />
            </div>
        </div>
    `,
};
