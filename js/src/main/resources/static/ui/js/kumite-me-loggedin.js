import { ref, computed } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
import { Tooltip } from "bootstrap";

import KumiteAccountRef from "./kumite-account-ref.js";
import KumitePlayerRef from "./kumite-player-ref.js";

import KumiteMeRefreshToken from "./kumite-me-refresh_token.js";

export default {
	components: {
		KumiteAccountRef,
		KumitePlayerRef,
		KumiteMeRefreshToken,
	},
	computed: {
		...mapState(useKumiteStore, ["nbAccountFetching", "account", "isLoggedIn"]),
		...mapState(useKumiteStore, {
			players(store) {
				return Object.values(store.players).filter((p) => p.accountId == this.account.accountId);
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadCurrentAccountPlayers();

		const countries = ref({});
		// https://flagpedia.net/download/api
		fetch("https://flagcdn.com/en/codes.json")
			.then((response) => {
				if (!response.ok) {
					console.warn("Issue downloading countries");
				}
				return response.json();
			})
			.then((json) => {
				countries.value = json;
			});

		const countryCode = computed(() => store.account.raw.countryCode || "unknown");

		const updateCountry = function (newCountryCode) {
			console.log("Update accouht country", newCountryCode);

			// Update the store asap
			store.account.raw.countryCode = newCountryCode;

			const userUpdates = {};
			userUpdates.countryCode = newCountryCode;

			store.fetchCsrfToken().then((csrfToken) => {
				const headers = {};
				headers[csrfToken.header] = csrfToken.token;
				headers["Content-Type"] = "application/json";

				try {
					const fetchOptions = {
						method: "POST",
						headers: headers,
						body: JSON.stringify(userUpdates),
					};
					const response = fetch("/api/login/v1/user", fetchOptions);
					if (!response.ok) {
						throw store.newNetworkError("POST for userUpdate has failed ", "/api/login/v1/user", response);
					}

					const updatedUser = response.json();

					// The submitted move may have impacted the user
					store.$patch((state) => {
						state.account = updatedUser;
					});
				} catch (e) {
					store.onSwallowedError(e);
				}
			});
		};

		if (countryCode.value === "unknown") {
			console.log("The account has no countryCode");
			// https://www.techighness.com/post/get-user-country-and-region-on-browser-with-javascript-only/
			fetch("https://unpkg.com/moment-timezone/data/meta/latest.json")
				.then((response) => {
					if (!response.ok) {
						console.warn("Issue downloading timezone info");
					}
					return response.json();
				})
				.then((json) => {
					console.debug("timezones", json);

					const userTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone;
					const countryCodes = json.zones[userTimezone].countries;

					console.log("countryCodes", countryCodes);

					if (countryCodes.length === 0) {
						console.warn("No country for timezone", userTimezone);
						return;
					}

					if (countryCode.value === "unknown") {
						updateCountry(countryCodes[0]);
					}
				});
		}

		return { countryCode, countries, updateCountry };
	},
	template: /* HTML */ `
        <span>
            <KumiteAccountRef :accountId="account.accountId" /><br />
            <span v-if="account.raw">
                raw.raw={{account.raw.rawRaw}}<br />
                username={{account.raw.username}}<br />
                name={{account.raw.name}}<br />
                email={{account.raw.email}}<br />
            </span>

            <div>
                <div class="col my-auto">
                    <span class="btn-group ">
                        <button type="button" class="btn btn-outline-secondary dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
                            Current country: {{countries[countryCode] || countryCode}}
                            <img
                                v-if="countryCode != 'unknown'"
                                :src="'https://flagcdn.com/' + countryCode.toLowerCase() + '.svg'"
                                :alt="countryCode"
                                width="48"
                                height="36"
                            />
                        </button>
                        <ul class="dropdown-menu">
                            <li>
                                <a
                                    class="dropdown-item"
                                    @click="updateCountry(countryCode)"
                                    :data-testid="'country_' + countryIndex"
                                    v-for="(countryName, countryCode, countryIndex) in countries"
                                >
                                    <img
                                        :src="'https://flagcdn.com/' + countryCode.toLowerCase() + '.svg'"
                                        :alt="countryCode"
                                        width="48"
                                        height="36"
                                    />{{countryName}}
                                </a>
                            </li>
                        </ul>
                    </span>
                </div>
            </div>

            You manage {{players.length}} players:
            <ul>
                <li v-for="player in players"><KumitePlayerRef :playerId="player.playerId" /></li>
            </ul>

            <KumiteMeRefreshToken />
        </span>
    `,
};
