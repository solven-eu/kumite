// my-component.js
import { ref } from 'vue'
import KumiteContest from './kumite-contest.js'

export default {
// https://vuejs.org/guide/components/registration#local-registration
components: {
  KumiteContest
},
  setup(props) {
	const error = ref({});
	const isLoading = ref(true);
	const contests = ref({
	  contests: []
	});

	async function theData(url) {
	  try {
	    isLoading.value = true;
	    const response = await fetch(url);
	    const responseJson = await response.json();
	    contests.value = responseJson
	  } catch(e) {
	    console.error('Issue on Network: ', e)
		error.value = e;
	  } finally {
	    isLoading.value = false;
	  }
	};

	theData('/api/contests?game_id=' + props.gameId)
	
    return {isLoading, contests }
  },
  // https://vuejs.org/guide/components/props.html
  props: {
    gameId: 	{
	    type: String,
	    required: true
	  },
  },
  template: `
  <div v-if="isLoading">
  	Loading contests
	</div>
	<div v-else>
	  <li v-for="contest in contests">
		<KumiteContest :gameId="gameId" :contestId="contest.contestId"/>
	  </li>
  </div>
  `
}