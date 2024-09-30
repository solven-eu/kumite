import { mapState } from "pinia";
import { useUserStore } from "./store-user.js";

import KumiteMeLoggedIn from "./kumite-me-loggedin.js";

export default {
	components: {
		KumiteMeLoggedIn,
	},
	computed: {
		...mapState(useUserStore, ["isLoggedIn"]),
	},
	setup() {
		return {};
	},
	template: /* HTML */ `
        <div v-if="!isLoggedIn">You need to login</div>
        <div v-else>
            <KumiteMeLoggedIn />
        </div>
    `,
};
