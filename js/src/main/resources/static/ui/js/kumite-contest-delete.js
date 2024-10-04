import { ref, onMounted, onUnmounted } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	props: {
		contestId: {
			type: String,
			required: true,
		},
		gameId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadContestIfMissing(props.contestId, props.gameId);
        
        const doGameover = function() {
            console.log("DELETE contest", contestId);

            async function deleteFromUrl(url) {
                try {
                    const fetchOptions = {
                        method: "DELETE",
                        headers: { "Content-Type": "application/json" },
                    };
                    const response = await store.authenticatedFetch(url, fetchOptions);
                    if (!response.ok) {
                        throw store.newNetworkError("Rejected DELETE for move for contestId=" + contestId, url, response);
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

            return deleteFromUrl(`/contests?contest_id=${props.contestId}`);
        };

		return {doGameover};
	},
	template: /* HTML */ ` <button class="btn btn-danger" @click="doGameover">Archive this contest (force gameOver)</button> `,
};
