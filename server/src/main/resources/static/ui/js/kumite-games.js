// my-component.js
import { ref } from 'vue'
import KumiteGame from './kumite-game.js'

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
	  KumiteGame
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

	theData('/api/games')
	
    return {isLoading, games }
  },
  template: `
  <div v-if="isLoading">
  	Loading games
	</div>
	<div v-else>
	  <li v-for="game in games">
	  	<RouterLink :to="{path:'/games/' + game.gameId}">{{game.title}}</RouterLink>
	  	<KumiteGame :gameId="game.gameId" :game="game"/>
	  </li>
  </div>
  `
}