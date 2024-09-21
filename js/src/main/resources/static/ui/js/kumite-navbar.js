import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	computed: {
		...mapState(useKumiteStore, ["needsToLogin", "account", "tokens", "nbAccountFetching", "playingPlayerId"]),
	},
	setup() {
		const store = useKumiteStore();

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
        <nav class="navbar navbar-expand-lg navbar-light bg-light">
            <div class="container-fluid">
                <RouterLink class="navbar-brand" to="/">Kumite</RouterLink>
                <button
                    class="navbar-toggler"
                    type="button"
                    data-bs-toggle="collapse"
                    data-bs-target="#navbarSupportedContent"
                    aria-controls="navbarSupportedContent"
                    aria-expanded="false"
                    aria-label="Toggle navigation"
                >
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarSupportedContent">
                    <ul class="navbar-nav me-auto mb-2 mb-lg-0">
                        <li class="nav-item">
                            <RouterLink class="nav-link active" aria-current="page" to="/">Home</RouterLink>
                        </li>
                        <li class="nav-item">
                            <RouterLink class="nav-link" to="/html/games">Games</RouterLink>
                        </li>
                        <li class="nav-item">
                            <RouterLink class="nav-link" to="/html/contests">Contests</RouterLink>
                        </li>
                        <li class="nav-item">
                            <RouterLink class="nav-link" to="/html/about">About</RouterLink>
                        </li>

                        <li class="nav-item" v-if="account.raw">
                            <RouterLink class="nav-link" to="/html/me">Account Settings</RouterLink>
                        </li>
                    </ul>
                    <span v-if="account.raw">
                        Current user: {{account.raw.name}}<img
                            :src="account.raw.picture"
                            class="img-thumbnail"
                            alt="You're looking nice"
                            width="128"
                            height="128"
                            v-if="account.raw.picture"
                        />
                    </span>
                </div>
            </div>
        </nav>
    `,
};
