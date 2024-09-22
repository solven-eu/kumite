import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import LoginOptions from "./login-providers.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		LoginOptions,
	},
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "needsToLogin"]),
		...mapState(useKumiteStore, {
			user(store) {
				return store.account;
			},
		}),
	},
	setup() {
		const store = useKumiteStore();

		store.loadUser();

		return {};
	},
	template: /* HTML */ `
        <div v-if="needsToLogin">
            <div v-if="nbAccountFetching > 0">Loading...</div>
            <div v-else>
                <LoginOptions />
            </div>
        </div>
        <div v-else>Welcome {{user.raw.name}}. ?Logout?</div>
    `,
};
