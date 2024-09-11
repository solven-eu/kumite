import { ref, onUnmounted } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteBoardJoined from "./kumite-board-joined.js";

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		KumiteBoardJoined,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		contestId: {
			type: String,
			required: true,
		},
		gameId: {
			type: String,
			required: true,
		},
		showCurl: {
			type: Boolean,
			default: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, [
			"nbGameFetching",
			"nbContestFetching",
			"nbBoardFetching",
			"playingPlayerId",
		]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
			board(store) {
				return store.boards[this.contestId];
			},
		}),
		curlGetBoard() {
			return (
				"curl " +
				window.location.protocol +
				"//" +
				window.location.host +
				"/api/board?contest_id=" +
				this.contestId
			);
		},
	},
	setup(props) {
		const store = useKumiteStore();

		// We load current accountPlayers to enable player registration
		store.loadCurrentAccountPlayers();

		const isLoading = ref(false);

		const canJoinAsPlayer = ref(false);

		const hasJoinedAsPlayer = ref(false);
		const isJoiningAsPlayer = ref(false);
		function joinAsPlayer() {
			const playerId = store.playingPlayerId;
			const contestId = props.contestId;

			isJoiningAsPlayer.value = true;

			async function fetchFromUrl(url) {
				store.nbBoardOperating++;

				try {
					// console.log("Fetch headers:", store.apiHeaders);
					const response = await store.authenticatedFetch(url, {
						method: "POST",
					});
					if (!response.ok) {
						throw new Error(
							"Rejected request for joining as player url " + url,
						);
					}
					const responseJson = await response.json();

					return responseJson;
				} catch (e) {
					console.error("Issue on Network: ", e);
					isJoiningAsPlayer.value = false;
					hasJoinedAsPlayer.value = false;
					throw e;
				} finally {
					store.nbBoardOperating--;
				}
			}

			return fetchFromUrl(
				`/api/board/player?player_id=${playerId}&contest_id=${contestId}&viewer=false`,
			).then((playingPlayer) => {
				console.debug("playingPlayer", playingPlayer);
				isJoiningAsPlayer.value = false;
				hasJoinedAsPlayer.value = true;
			});
		}

		const hasJoinedAsViewer = ref(false);
		const isJoiningAsViewer = ref(false);
		function joinAsViewer() {
			throw Error("Not implemented yet");
		}

		isLoading.value = false;
		store
			.loadBoard(props.gameId, props.contestId)
			.then((contestView) => {
				console.debug("contestView", contestView);

				if (contestView.playerCanJoin) {
					canJoinAsPlayer.value = true;
				}

				if (contestView.playerHasJoined) {
					hasJoinedAsPlayer.value = true;
				}

				if (contestView.accountIsViewing) {
					hasJoinedAsViewer.value = true;
				}
			})
			.finally(() => {
				isLoading.value = false;
			});

		return {
			isLoading,
			canJoinAsPlayer,

			hasJoinedAsPlayer,
			isJoiningAsPlayer,
			joinAsPlayer,
			hasJoinedAsViewer,
			isJoiningAsViewer,
			joinAsViewer,
		};
	},
	template: `
	<div class="border" v-if="contest">
		<!-- Loading metadata -->
		<div v-if="isLoading">
		   Loading {{isLoading}}
		</div>
	   <!-- IsJoining loader -->
	   <div v-else-if="isJoiningAsPlayer || isJoiningAsViewer">
	      Joining {{isJoiningAsPlayer || isJoiningAsViewer}}
	   </div>
	   <!-- Joined as player -->
	   <div v-else-if="hasJoinedAsPlayer">
	   	<KumiteBoardJoined :gameId="gameId" :contestId="contestId" />
	   </div>
	   <!-- Joined as viewer -->
	   <div v-else-if="hasJoinedAsViewer">
	      hasJoinedAsViewer={{hasJoinedAsViewer}}
	   </div>
	   <!-- Joining options -->
	   <div v-else>
	      <!-- Offer to join as player-->
	      <div v-if="canJoinAsPlayer">
	         <button type="button" @click="joinAsPlayer()"  class="btn btn-outline-primary">Join contest as player</button>
	         <!--span v-if="exampleMovesMetadata.error" class="alert alert-warning" role="alert">{{exampleMovesMetadata.error}}</span-->
	      </div>
	      <!-- Offer to join as viewer -->
	      <div>
	         <button type="button" @click="joinAsViewer()"  class="btn btn-outline-primary" disabled>Join contest as viewer</button>
	         <!--span v-if="exampleMovesMetadata.error" class="alert alert-warning" role="alert">{{exampleMovesMetadata.error}}</span-->
	      </div>
	   </div>
	</div>
  `,
};