import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	props: {
		accountId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "needsToLogin"]),
		...mapState(useKumiteStore, {
			player(store) {
				return store.players[this.playerId] || { status: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.ensureUser();

		return {};
	},
	template: /* HTML */ `
        <RouterLink :to="{path:'/html/me'}">
            <i class="bi bi-person"></i>accountId: {{ accountId }} <span v-if="account.accountId === accountId">You</span>
        </RouterLink>
    `,
};
