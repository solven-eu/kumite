import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

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
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "needsToLogin"]),
		...mapState(useKumiteStore, {
			player(store) {
				return store.players[this.playerId] || { error: "not_loaded" };
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
	template: /* HTML */ `
        <div v-if="needsToLogin">You need to login</div>
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
