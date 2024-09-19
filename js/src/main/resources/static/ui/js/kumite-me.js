// my-component.js
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
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "needsToLogin"]),
		...mapState(useKumiteStore, {
			players(store) {
				return Object.values(store.players).filter((p) => p.accountId == this.account.accountId);
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadCurrentAccountPlayers();

		return {};
	},
	template: /* HTML */ `
        <div v-if="needsToLogin">You need to login</div>
        <div v-else>
<KumiteAccountRef :accountId="account.accountId" /><br/>
            <span v-if="account.raw">
                raw.raw={{account.raw.rawRaw}}<br />
                accountUSername={{account.raw.username}}<br />
                name={{account.raw.name}}<br />
                email={{account.raw.email}}<br />
            </span>
            <ul>
<li v-for="player in players"><KumitePlayerRef :playerId="player.playerId"/></li>
            </ul>
        </div>
    `,
};
