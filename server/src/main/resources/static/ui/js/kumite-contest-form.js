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
				return store.games[this.gameId] || {'error': 'not_loaded'};
			},
		}),
	},
	method: {
		submitContestForm: function() {
			const payload = {'constant_metadata': {'name': }};
			
			console.log("Submitting contestCreation", constantMetadata);

			async function postFromUrl(url) {
				try {
					const fetchOptions = {
						method: "POST",
						headers: { "Content-Type": "application/json" },
						body: JSON.stringify(move),
					};
					const response = await store.authenticatedFetch(url, fetchOptions);
					if (!response.ok) {
						throw new NetworkError(
							"Rejected POST for move for games url=" + url,
							url,
							response,
						);
					}

					// debugger;
					// context.emit('move-sent', {gameId: props.gameId, contestId: props.contestId});
					// The submitted move may have impacted the leaderboard
					store.$patch((state) => {
						if (!state.leaderboards[contestId]) {
							state.leaderboards[contestId] = {};
						}
						state.leaderboards[contestId].stale = true;
					});
				} catch (e) {
					console.error("Issue on Network:", e);
				}
			}

			const playerId = store.playingPlayerId;
			return postFromUrl(
				`/api/board/move?contest_id=${contestId}&player_id=${playerId}`,
			);
		},
	}
	setup(props) {
		const store = useKumiteStore();

		return {};
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
	    <input type="text" class="form-control" id="contestName" aria-describedby="emailHelp">
	    <div id="emailHelp" class="form-text">Pick a name so your friends can find your contest.</div>
	  </div>
	  <button type="submit" @click="submitContestForm" class="btn btn-primary">Submit</button>
	</form>
</div>
  `,
};
