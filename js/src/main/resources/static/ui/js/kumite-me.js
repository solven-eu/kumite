import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
import { Tooltip } from "bootstrap";

import KumiteAccountRef from "./kumite-account-ref.js";
import KumitePlayerRef from "./kumite-player-ref.js";

import KumiteMeRefreshToken from "./kumite-me-refresh_token.js";

export default {
	components: {
		KumiteAccountRef,
		KumitePlayerRef,
		KumiteMeRefreshToken,
	},
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "isLoggedIn"]),
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
        <div v-if="!isLoggedIn">You need to login</div>
        <div v-else>
            <KumiteAccountRef :accountId="account.accountId" /><br />
            <span v-if="account.raw">
                raw.raw={{account.raw.rawRaw}}<br />
                username={{account.raw.username}}<br />
                name={{account.raw.name}}<br />
                email={{account.raw.email}}<br />
            </span>

            You manage {{players.length}} players:
            <ul>
                <li v-for="player in players"><KumitePlayerRef :playerId="player.playerId" /></li>
            </ul>

            <KumiteMeRefreshToken />
        </div>
    `,
};
