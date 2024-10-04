import { ref, watch } from "vue";

import { mapState } from "pinia";
import { useUserStore } from "./store-user.js";

import { useRouter } from "vue-router";

import LoginOptions from "./login-providers.js";
import Logout from "./login-logout.js";

import Whatnow from "./whatnow.js";

export default {
	components: {
		LoginOptions,
		Logout,
		Whatnow,
	},
	props: {
		logout: {
			type: String,
			required: false,
		},
	},
	computed: {
		...mapState(useUserStore, ["nbAccountFetching", "account", "isLoggedIn", "isLoggedOut"]),
		...mapState(useUserStore, {
			user(store) {
				return store.account;
			},
		}),
	},
	setup(props) {
		const userStore = useUserStore();

		userStore.loadUser();

		return {};
	},
	template: /* HTML */ `
        <div v-if="isLoggedIn">
            Welcome {{user.details.name}}. <Logout />

            <Whatnow />
        </div>
        <div v-else-if="isLoggedOut">
            <LoginOptions />
        </div>
        <div v-else>
            <div v-if="nbAccountFetching > 0">Loading...</div>
            <div v-else>?</div>
        </div>
    `,
};
