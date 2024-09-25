import { ref, watch } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	props: {
		logout: {
			type: String,
			required: false,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["needsToCheckLogin", "nbAccountFetching", "isLoggedIn", "isLoggedOut"]),
	},
	setup(props) {
		return {};
	},
	template: /* HTML */ `
        <span v-if="isLoggedIn" hidden>You are logged in</span>
        <span v-if="isLoggedOut">
            <RouterLink :to="{path:'/html/login'}"><i class="bi bi-person"></i> You need to login</RouterLink>
        </span>
        <span v-else-if="needsToCheckLogin">
            <span v-if="nbAccountFetching > 0">Loading account</span>
            <span v-else>Unclear login status but not loading. Should not happen</span>
        </span>
        <span v-else> This should not happen (login-ref.js) </span>
    `,
};
