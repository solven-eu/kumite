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
		...mapState(useKumiteStore, ["account"]),
		...mapState(useKumiteStore, {
			player(store) {
				return store.players[this.playerId] || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadUser();

		return {};
	},
	template: /* HTML */ `
        <RouterLink :to="{path:'/html/me'}">
            <i class="bi bi-person"></i>accountId: {{ accountId }}<span v-if="account.accountId === accountId"> (You)</span>
            <img
                v-if="account.countryCode"
                :src="'https://flagcdn.com/' + account.countryCode.toLowerCase() + '.svg'"
                :alt="account.countryCode"
                width="48"
                height="36"
            />
        </RouterLink>
    `,
};
