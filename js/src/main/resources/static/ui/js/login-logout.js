import { ref, watch } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import { useRouter } from "vue-router";

import LoginOptions from "./login-providers.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		LoginOptions,
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
		const router = useRouter();

		store.loadUser();

		const doLogout = function () {
			console.info("Logout");
			async function fetchFromUrl(url, csrfToken) {
				// https://stackoverflow.com/questions/60265617/how-do-you-include-a-csrf-token-in-a-vue-js-application-with-a-spring-boot-backe
				const headers = { [csrfToken.header]: csrfToken.token };

				try {
					const response = await fetch(url, {
						method: "POST",
						headers: headers,

						// https://stackoverflow.com/questions/39735496/redirect-after-a-fetch-post-call
						// https://github.com/whatwg/fetch/issues/601#issuecomment-502667208
						redirect: "follow",
					});
					if (!response.ok) {
						throw new Error("Rejected request for logout");
					}

					const json = await response.json();

					// We we can not intercept 3XX to extract the Location header, we introduced an API providing the Location as body of a 2XX.
					const logoutHtmlRoute = json["Location"];

					console.info("Redirect to logout route", logoutHtmlRoute);

					router.push(logoutHtmlRoute);

					// We force reloading the page to take in account the removed SESSION
					// There should be a cleaner way to do it, without full-reload
					router.go(0);
				} catch (e) {
					console.error("Issue on Network: ", e);
				}
			}

			store.fetchCsrfToken().then((csrfToken) => {
				fetchFromUrl(`/logout`, csrfToken);
			});
		};

		return { doLogout };
	},
	template: /* HTML */ `
        <span v-if="!needsToLogin">
            <button class="btn btn-danger" @click="doLogout">Logout</button>
        </span>
    `,
};
