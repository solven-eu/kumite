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
		someId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, {
			account(store) {
				return store.accounts[this.someId] || { error: "not_loaded" };
			},
			player(store) {
				return store.players[this.someId] || { error: "not_loaded" };
			},
			game(store) {
				return store.games[this.someId] || { error: "not_loaded" };
			},
			contest(store) {
				return store.contests[this.someId] || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		const idValidUuid = function (id) {
			// https://stackoverflow.com/questions/7905929/how-to-test-valid-uuid-guid
			// const uuidRegex =             /^[0-9a-f]{8}-[0-9a-f]{4}-[0-5][0-9a-f]{3}-[089ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
			// We go with a simpler regex for now, as randomPlayer and fakePlayer has seemingly invalid UUIDs
			const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

			return uuidRegex.test(id);
		};

		if (!idValidUuid(props.someId)) {
			console.warn("Invalid id in search", props.someId);
			return;
		}

		console.log("Search for", props.someId);

		store.loadAccountIfMissing(props.someId);
		store.loadPlayerIfMissing(props.someId);
		store.loadGameIfMissing(props.someId);
		store.loadContestIfMissing(props.someId);

		return {};
	},
	template: /* HTML */ `
        <div>
            <ul>
                <li v-if="!account.error">account=<KumiteAccountRef :accountId="someId" /></li>
                <li v-if="!player.error">player=<KumitePlayerRef :playerId="someId" /></li>
                <li v-if="!game.error">game={{game}}</li>
                <li v-if="!contest.error">contest={{contest}}</li>
            </ul>
        </div>
    `,
};
