import { mapState } from "pinia";
import { useUserStore } from "./store-user.js";

import Logout from "./login-logout.js";

export default {
	components: {
		Logout,
	},
	computed: {
		...mapState(useUserStore, ["isLoggedIn", "account", "tokens", "nbAccountFetching", "playingPlayerId"]),
	},
	setup() {
		const userStore = useUserStore();

		userStore.loadUser();

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
                            <RouterLink class="nav-link" to="/html/games"><i class="bi bi-puzzle" />Games</RouterLink>
                        </li>
                        <li class="nav-item">
                            <RouterLink class="nav-link" to="/html/contests"><i class="bi bi-trophy" />Contests</RouterLink>
                        </li>
                        <li class="nav-item">
                            <RouterLink class="nav-link" to="/html/about"><i class="bi bi-info-lg" />About</RouterLink>
                        </li>

                        <li class="nav-item" v-if="isLoggedIn">
                            <RouterLink class="nav-link" to="/html/me"><i class="bi bi-person" />Account Settings</RouterLink>
                        </li>
                    </ul>
                    <span v-if="isLoggedIn">
                        {{account.raw.name}}<img
                            :src="account.raw.picture"
                            class="img-thumbnail"
                            alt="You're looking nice"
                            width="64"
                            height="64"
                            v-if="account.raw.picture"
                        />
                        <Logout />
                    </span>
                </div>
            </div>
        </nav>
    `,
};
