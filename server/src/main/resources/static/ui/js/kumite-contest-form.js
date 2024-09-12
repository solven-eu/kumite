// my-component.js
import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteGameHeader from "./kumite-game-header.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteGameHeader,
	},
	// https://vuejs.org/guide/components/props.html
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

		const contestName = ref("A nice name for a contest");

		const submitContestForm = function () {
			const payload = { constant_metadata: { name: this.contestName } };

			console.log("Submitting contestCreation", constantMetadata);

			async function postFromUrl(url) {
				try {
					const fetchOptions = {
						method: "POST",
						headers: { "Content-Type": "application/json" },
						body: JSON.stringify(payload),
					};
					const response = await store.authenticatedFetch(url, fetchOptions);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected POST for move for games url=" + url,
							url,
							response,
						);
					}

					const contest = await response.json();

					{
						console.log("Registering contestId", contest.contestId);
						store.$patch({
							contests: { ...store.contests, [contest.contestId]: contest },
						});
					}
				} catch (e) {
					console.error("Issue on Network:", e);
				}
			}

			const playerId = store.playingPlayerId;
			return postFromUrl(`/api/contests?game_id=${gameId}`);
		};

		return { contestName, submitContestForm };
	},
	template: `
<div v-if="(!game)">
	<div v-if="(nbGameFetching > 0)" >
		<div class="spinner-border" role="status">
		  <span class="visually-hidden">Loading gameId={{gameId}}</span>
		</div>
	</div>
	<div v-else>
		{{ game.error }}
	</div>
</div>
<div v-else>
	<KumiteGameHeader :gameId="gameId" v-if="showGame" />
	
	<form>
	  <div class="mb-3">
	    <label for="contestName" class="form-label">Email address</label>
	    <input type="text" class="form-control" id="contestName" v-model="contestName" aria-describedby="emailHelp">
	    <div id="emailHelp" class="form-text">Pick a name so your friends can find your contest.</div>
	  </div>
	  <button type="submit" @click="submitContestForm" class="btn btn-primary">Submit</button>
	</form>
</div>
  `,
};
