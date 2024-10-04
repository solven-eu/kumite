import { mapState } from "pinia";
import { useUserStore } from "./store-user.js";

import LoginRef from "./login-ref.js";
import KumiteMeLoggedIn from "./kumite-account-me-loggedin.js";

export default {
	components: {
		LoginRef,
		KumiteMeLoggedIn,
	},
	computed: {
		...mapState(useUserStore, ["isLoggedIn"]),
	},
	setup() {
		return {};
	},
	template: /* HTML */ `
        <div v-if="!isLoggedIn"><LoginRef /></div>
        <div v-else>
            <KumiteMeLoggedIn />
        </div>
    `,
};
