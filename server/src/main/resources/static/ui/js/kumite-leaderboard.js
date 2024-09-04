import { ref } from "vue";
import { useKumiteStore } from "./store.js";

export default {
	setup(props) {
		const errorRef = ref({});
		const isLoading = ref(true);
		const leaderboard = ref({
			playerScores: [],
		});

		const store = useKumiteStore();

		async function theData(url) {
			isLoading.value = true;
			const responseJson = await store
				.authenticatedFetch(url)
				.then((response) => {
					if (response.ok) {
						return response.json();
					}
					console.error("Issue on Fetch", url, response.status);
					throw new Error("Something went wrong");
				})
				.then((leaderboardNetworkd) => {
					leaderboard.value = leaderboardNetworkd;
				})
				.catch((error) => {
					console.log(error);
					errorRef.value = error;
				})
				.finally(() => {
					isLoading.value = false;
				});
		}

		theData("/api/leaderboards?contest_id=" + props.contestId);

		return { isLoading, errorRef, leaderboard };
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		contestId: {
			type: String,
			required: true,
		},
	},
	template: `
<div v-if="isLoading">
  	Loading leaderboard
</div>
<div v-else-if="errorRef.message">
	error={{errorRef}}
</div>
<div v-else>
		<div v-if="leaderboard.playerScores.length">
		leaderboard={{leaderboard}}
	  <li v-for="item in leaderboard.playerScores">
	    {{item}}
	  </li>
	  </div>
	  <div v-else>
	  	Leaderboard is empty
	  </div>
</div>
  `,
};
