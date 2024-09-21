import {ref} from "vue";

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
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadCurrentAccountPlayers();
        
        const refreshToken = ref("");

        const generateRefreshToken = function() {
            console.debug("Generating a refresh_token");
            async function fetchFromUrl(url) {
                try {
                    const response = await fetch(url);
                    if (!response.ok) {
                        throw new Error("Rejected request for games url" + url);
                    }

                    const responseJson = await response.json();
                    const refreshTokenWrapper = responseJson;

                    console.info("refreshToken", refreshTokenWrapper);

                    refreshToken.value = refreshTokenWrapper;
                } catch (e) {
                    console.error("Issue on Network: ", e);
                    exampleMovesMetadata.value.error = e;
                }
            }

            fetchFromUrl(`/oauth2/token?refresh_token=true`);
        };

        return {generateRefreshToken, refreshToken};
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


            <form>
                You want to develop your own long-running robot? 
                <button type="button" @click="generateRefreshToken" class="btn btn-primary">Generate an refresh_token</button>
            </form>
            <div v-if="refreshToken">
                refresh_token=`{{refreshToken.refresh_token}}`<br/>
                (refreshToken)<br/>
                (Save it now on your side, as it will not be saved in Kumite-Server)
            </div>
        </div>
    `,
};
