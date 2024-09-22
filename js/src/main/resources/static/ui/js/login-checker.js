import { ref, watch } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import { useRouter } from "vue-router";

import LoginOptions from "./login-providers.js";
import Logout from "./login-logout.js";

export default {
	components: {
		LoginOptions,
        Logout,
	},
	props: {
		logout: {
			type: String,
			required: false,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "needsToLogin"]),
		...mapState(useKumiteStore, {
			user(store) {
				return store.account;
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadUser();

		return { };
	},
	template: /* HTML */ `
        <div v-if="needsToLogin">
            <div v-if="nbAccountFetching > 0">Loading...</div>
            <div v-else>
                <LoginOptions />
            </div>
        </div>
        <div v-else>
            Welcome {{user.raw.name}}. <Logout />
        </div>
    `,
};
