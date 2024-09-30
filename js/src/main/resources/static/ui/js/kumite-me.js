import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteMeLoggedIn from "./kumite-me-loggedin.js";

export default {
components: {
    KumiteMeLoggedIn,
},
computed: {
    ...mapState(useKumiteStore, ["isLoggedIn"]),
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
