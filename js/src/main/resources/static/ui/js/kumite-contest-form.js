// my-component.js
import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteContestRef from "./kumite-contest-ref.js";
import KumiteGameHeader from "./kumite-game-header.js";

export default {
	components: {
		KumiteContestRef,
		KumiteGameHeader,
	},
	props: {
		gameId: {
			type: String,
			required: true,
		},
		showGame: {
			type: Boolean,
			default: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId] || { error: "not_loaded" };
			},
		}),
	},
	method: {},
	setup(props) {
		const store = useKumiteStore();

		store.loadGameIfMissing(props.gameId);

		const contestName = ref("");
		const createdContest = ref({});

		const submitContestForm = function () {
			const constantMetadata = {
				constant_metadata: { name: contestName.value },
			};

			console.log("Submitting contestCreation", constantMetadata);

			async function postFromUrl(url) {
				try {
					const fetchOptions = {
						method: "POST",
						headers: { "Content-Type": "application/json" },
						body: JSON.stringify(constantMetadata),
					};
					const response = await store.authenticatedFetch(url, fetchOptions);
					if (!response.ok) {
						throw new Error("Rejected POST for move for games url=" + url);
					}

					const contest = await response.json();
					console.log("Created contest", contest);

					{
						console.log("Registering contestId", contest.contestId);
						store.$patch({
							contests: {
								...store.contests,
								[contest.contestId]: contest,
							},
						});
					}

					createdContest.value = contest;
				} catch (e) {
					console.error("Issue on Network:", e);
				}
			}

			return postFromUrl(`/contests?game_id=${props.gameId}`);
		};

		return { contestName, submitContestForm, createdContest };
	},
	template: /* HTML */ `
        <div v-if="(!game) || game.error">
            <div v-if="(nbGameFetching > 0)">
                <div class="spinner-border" role="status">
                    <span class="visually-hidden">Loading gameId={{gameId}}</span>
                </div>
            </div>
            <div v-else>{{ game.error }}</div>
        </div>

        <div v-if="createdContest.contestId">
            <KumiteContestRef :gameId="gameId" :contestId="createdContest.contestId" />
        </div>
        <div v-else>
            <KumiteGameHeader :gameId="gameId" v-if="showGame" />

            <form>
                <div class="mb-3">
                    <label for="contestName" class="form-label">Contest name</label>
                    <input type="text" class="form-control" id="contestName" v-model="contestName" aria-describedby="emailHelp" />
                    <div id="emailHelp" class="form-text">Pick a name so your friends can find your contest.</div>
                </div>
                <button type="button" @click="submitContestForm" class="btn btn-primary">Submit</button>
            </form>
        </div>
    `,
};
