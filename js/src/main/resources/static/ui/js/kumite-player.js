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
				return store.players[this.playerId] || { status: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadCurrentAccountPlayers().then(() => {
			store.loadPlayer(props.playerId);
		});
        
        const generateAccessToken = function() {
            console.debug("Generating a long-lived access_token");
            async function fetchFromUrl(url) {
                try {
                    const response = await store.authenticatedFetch(url);
                    if (!response.ok) {
                        throw new Error("Rejected request for games url" + url);
                    }

                    const responseJson = await response.json();
                    const newExampleMoves = responseJson.moves;

                    console.info("Loaded example moves", responseJson);

                    // This convoluted `modify` is needed until we clarify how wo can edit the Ref from this method
                    // https://stackoverflow.com/questions/26957719/replace-object-value-without-replacing-reference
                    function modify(obj, newObj) {
                        Object.keys(obj).forEach(function (key) {
                            delete obj[key];
                        });

                        Object.keys(newObj).forEach(function (key) {
                            obj[key] = newObj[key];
                        });
                    }

                    exampleMovesMetadata.value.loaded = true;

                    // https://stackoverflow.com/questions/61452458/ref-vs-reactive-in-vue-3
                    modify(exampleMoves, newExampleMoves);
                } catch (e) {
                    console.error("Issue on Network: ", e);
                    exampleMovesMetadata.value.error = e;
                }
            }

            // const viewingPlayerId = "00000000-0000-0000-0000-000000000000";
            // const playerId = viewingPlayerId;
            const playerId = store.playingPlayerId;
            fetchFromUrl(`/board/moves?contest_id=${props.contestId}&player_id=${playerId}`);
        };

		return {generateAccessToken};
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
            <div v-else>
            This is one of your players.
            

            <form>
                <button type="button" @click="generateAccessToken" class="btn btn-primary">Generate an access_token</button>
            </form>
            </div>
        </div>
    `,
};
