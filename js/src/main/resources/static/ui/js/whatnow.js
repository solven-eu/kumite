import { mapState } from "pinia";

import { useUserStore } from "./store-user.js";

import LoginRef from "./login-ref.js";

export default {
	components: {
		LoginRef,
	},
	computed: {
		...mapState(useUserStore, ["isLoggedIn"]),
	},
	setup() {
		return {};
	},
	template: /* HTML */ `
        <div v-if="!isLoggedIn"><LoginRef /></div>
        <span v-else>
            <ul>
                <li><RouterLink to="/html/games">Browse through games</RouterLink></li>
                <li><RouterLink to="/html/contests">Browse through contests</RouterLink></li>
                <li><RouterLink to="/html/me">Generate a refresh_token</RouterLink></li>
            </ul>
        </span>
    `,
};
