import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

import Flag from "./flag.js";

export default {
	components: {
		Flag,
	},
	props: {
		accountId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useUserStore, ["account"]),
		...mapState(useKumiteStore, {
			player(store) {
				return store.players[this.playerId] || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const userStore = useUserStore();

		userStore.loadUser();

		return {};
	},
	template: /* HTML */ `
        <RouterLink :to="{path:'/html/me'}">
            <i class="bi bi-person"></i>accountId: {{ accountId }}<span v-if="account.accountId === accountId"> (You)</span>

            <Flag :country="account.details.countryCode" v-if="account.details.countryCode" />
        </RouterLink>
    `,
};
