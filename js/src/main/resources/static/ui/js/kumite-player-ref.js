import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

export default {
	props: {
		playerId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useUserStore, ["nbAccountFetching", "account"]),
		...mapState(useKumiteStore, {
			player(store) {
				return store.players[this.playerId] || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();
		const userStore = useUserStore();

		userStore.loadCurrentAccountPlayers().then(() => {
			store.loadPlayer(props.playerId);
		});

		return {};
	},
	template: /* HTML */ `
        <RouterLink :to="{path:'/html/players/' + playerId}">
            <i class="bi bi-android"></i>playerId: {{ playerId }} <span v-if="account.accountId === player.accountId"> (You)</span></RouterLink
        >
    `,
};
