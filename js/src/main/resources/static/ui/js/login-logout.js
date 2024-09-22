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

		const csrfToken = ref({});
		const fetchCsrfToken = async function () {
			try {
				const response = await fetch(`/api/login/v1/csrf`);
				if (!response.ok) {
					throw new Error("Rejected request for logout");
				}

				const json = await response.json();
				const csrfHeader = json.header;
				console.log("csrf header", csrfHeader);

				const freshCrsfToken = response.headers.get(csrfHeader);
				if (!freshCrsfToken) {
					throw new Error("Invalid csrfToken");
				}
				console.debug("csrf", freshCrsfToken);

				csrfToken.value = { header: csrfHeader, token: freshCrsfToken };
			} catch (e) {
				console.error("Issue on Network: ", e);
			}
		};

		const doLogout = function () {
			console.info("Logout");
			async function fetchFromUrl(url) {
				// https://www.baeldung.com/spring-security-csrf
				// If we relied on Cookie, `.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())` we could get the csrfToken with:
				// const csrfToken = document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');

				// https://stackoverflow.com/questions/60265617/how-do-you-include-a-csrf-token-in-a-vue-js-application-with-a-spring-boot-backe
				const headers = { [csrfToken.value.header]: csrfToken.value.token };

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

			fetchCsrfToken().then(() => {
				fetchFromUrl(`/logout`);
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
