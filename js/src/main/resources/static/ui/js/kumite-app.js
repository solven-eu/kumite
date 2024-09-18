import { watch } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteNavbar from "./kumite-navbar.js";

export default {
	components: {
		KumiteNavbar,
	},
	computed: {
		...mapState(useKumiteStore, ["needsToLogin", "account", "tokens", "nbAccountFetching", "playingPlayerId"]),
	},
	setup() {
		const store = useKumiteStore();

		// https://pinia.vuejs.org/core-concepts/state.html
		// Bottom of the page: there is a snippet for automatic persistence in localStorage
		// We still need to reload from localStorage on boot
		watch(
			store.$state,
			(state) => {
				// persist the whole state to the local storage whenever it changes
				localStorage.setItem("kumiteState", JSON.stringify(state));
			},
			{ deep: true },
		);

		store
			.loadMetadata()
			.then(() => {
				return store.ensureUser();
			})
			.then(() => {
				return store.loadUserTokens();
			})
			.catch((error) => {
				store.onSwallowedError(error);
			});

		return {};
	},
	template: /* HTML */ `
        <div class="container">
            <KumiteNavbar />

            <main>
                <RouterView />
            </main>

            <div v-if="needsToLogin">
                <div v-if="$route.fullPath !== '/html/login'">
                    <RouterLink :to="{path:'/html/login'}"><i class="bi bi-person"></i> You need to login</RouterLink>
                </div>
            </div>

            <p v-else>
                <strong><i class="bi bi-person"></i>playerId:</strong> {{ playingPlayerId }}
            </p>
        </div>
    `,
};
