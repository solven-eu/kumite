import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";
import { useUserStore } from "./store-user.js";

import KumiteAccountRef from "./kumite-account-ref.js";
import KumitePlayerRef from "./kumite-player-ref.js";

export default {
	components: {
		KumiteAccountRef,
		KumitePlayerRef,
	},
	props: {
		playerId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "isLoggedIn"]),
		...mapState(useKumiteStore, {
			player(store) {
				return store.players[this.playerId] || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
    const store = useKumiteStore();
        const userStore = useUserStore();

        store.loadPlayer(props.playerId);
        
        // Why do we need to load current account players?
		userStore.loadCurrentAccountPlayers();

		return {};
	},
	template: /* HTML */ `
        <div v-if="!isLoggedIn">You need to login</div>
        <div v-else>
            <ul>
                <li><KumitePlayerRef :playerId="playerId" /></li>
            </ul>

            <div v-if="player.accountId && player.accountId !== account.accountId">
                This is a player managed by <KumiteAccountRef :accountId="player.accountId" />
            </div>
            <div v-else>This is one of your players.</div>
        </div>
    `,
};
