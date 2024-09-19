import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	props: {
		playerId: {
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

		store.loadCurrentAccountPlayers().then(() => {
			store.loadPlayer(props.playerId);
		});

		return {};
	},
	template: /* HTML */ ` <RouterLink :to="{path:'/html/players/' + playerId}"> <i class="bi bi-android"></i>playerId: {{ playerId }} </RouterLink> `,
};
