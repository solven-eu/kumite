// my-component.js
import { ref } from 'vue'
export default {
  setup(props) {
	const error = ref({});
	const isLoading = ref(true);
	const leaderboard = ref({
	  leaderboard: []
	});

	async function theData(url) {
	  try {
	    isLoading.value = true;
	    const response = await fetch(url);
	    const responseJson = await response.json();
	    leaderboard.value = responseJson
	  } catch(e) {
	    console.error('Issue on Network: ', e)
		error.value = e;
	  } finally {
	    isLoading.value = false;
	  }
	};

	theData('/api/leaderboards?contest_id=' + props.contestId)
	
    return {isLoading, leaderboard }
  },
  // https://vuejs.org/guide/components/props.html
  props: {
    contestId: 	{
	    type: String,
	    required: true
	  },
  },
  template: `
  <div v-if="isLoading">
  	Loading leaderboard
	</div>
	<div v-else>
		leaderboard={{leaderboard}}
	  <li v-for="item in leaderboard.playerScores">
	    {{item}}
	  </li>
  </div>
  `
}