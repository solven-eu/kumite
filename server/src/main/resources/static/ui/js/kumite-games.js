// my-component.js
import { ref } from 'vue'
import KumiteContests from './kumite-contests.js'

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
	  KumiteContests
	},
  setup() {
	const error = ref({});
	const isLoading = ref(true);
	const games = ref({
	  games: []
	});

	async function theData(url) {
	  try {
	    isLoading.value = true;
	    const response = await fetch(url);
	    const responseJson = await response.json();
	    games.value = responseJson
	  } catch(e) {
	    console.error('Issue on Network: ', e)
		error.value = e;
	  } finally {
	    isLoading.value = false;
	  }
	};

	theData('/games')
	
    return {isLoading, games }
  },
  template: `
  <div v-if="isLoading">
  	Loading games
	</div>
	<div v-else>
	  <li v-for="item in games">
	    {{item}}
		<KumiteContests :game_id="item.gameId"/>
	  </li>
  </div>
  `
}