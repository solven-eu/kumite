// my-component.js
import { ref } from 'vue'
import KumiteContests from './kumite-contests.js'

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
	  KumiteContests
	},
  setup(props) {
	const error = ref({});
	const isLoading = ref(true);
	const game = ref({
	  game: {}
	});

	// console.log("gameId", props.gameId);
	// console.log("game", props.game);
	
	if (!!props.game) {
		// We received the details as prop
		isLoading.value = false;
		game.value = props.game;
	} else {
		async function theData(url) {
		  try {
		    isLoading.value = true;
		    const response = await fetch(url);
		    const responseJson = await response.json();
		    game.value = responseJson[0]
		  } catch(e) {
		    console.error('Issue on Network: ', e)
			error.value = e;
		  } finally {
		    isLoading.value = false;
		  }
		};
		theData('/api/games?gameId=' + props.gameId)
	}
	
    return {isLoading, game }
  },
	// https://vuejs.org/guide/components/props.html
	props: {
	  gameId: 		{
		    type: String,
		    required: true
		  },
	  game: Object,
	},
  template: `
<div v-if="isLoading">
  	Loading games
</div>
<div v-else>
	Title: {{game.title}}<br/>
	Description: {{game.shortDescription}}<br/>
	<div v-for="ref in game.references">
		<a :href="ref" target="_blank">{{ref}}</a>
	</div>
	<KumiteContests :gameId="gameId"/>
</div>
  `
}