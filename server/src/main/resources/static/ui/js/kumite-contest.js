// my-component.js
import { ref } from 'vue'
import KumiteLeaderboard from './kumite-leaderboard.js'

export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
	  KumiteLeaderboard
	},
  setup(props) {
	const error = ref({});
	const isLoading = ref(0);
	const isLoaded = ref(0);
	const game = ref({
	  game: {}
	});
  const contest = ref({
    contest: {}
  });

	// console.log("gameId", props.gameId);
	// console.log("game", props.game);

	if (!!props.contest) {
		// We received the details as prop
		isLoaded.value++;
		contest.value = props.contest;
	} else {
		async function theData(url) {
		  try {
		    isLoading.value++;
		    const response = await fetch(url);
		    const responseJson = await response.json();
		    contest.value = responseJson[0]
		  } catch(e) {
		    console.error('Issue on Network: ', e)
			error.value = e;
		  } finally {
		    isLoaded.value++;
		  }
			//console.log(isLoading);
		};
		theData('/api/contests?game_id=' + props.gameId + "&contest_id=" + props.contestId)
	}

	if (!!props.game) {
		// We received the details as prop
		isLoaded.value++;
		game.value = props.game;
	} else {
		async function theData(url) {
		  try {
		    isLoading.value++;
		    const response = await fetch(url);
		    const responseJson = await response.json();
		    game.value = responseJson[0]
		  } catch(e) {
		    console.error('Issue on Network: ', e)
			error.value = e;
		  } finally {
		    isLoaded.value++;
		  }
		  //console.log(isLoading);
		};
		theData('/api/games?game_id=' + props.gameId)
	}
	
    return {isLoaded, contest, game }
  },
	// https://vuejs.org/guide/components/props.html
	props: {
	contestId: 		{
	    type: String,
	    required: true
	  },
	contest: Object,
	gameId: 		{
	    type: String,
	    required: true
	  },
	game: Object,
	},
  template: `
<div v-if="isLoaded < 2">
  	Loading contestId={{contestId}}
</div>
<div v-else>
	Game-Title: {{game.title}}<br/>
	Game-Description: {{game.shortDescription}}<br/>
	Contest-Name: {{contest.name}}<br/>
	<KumiteLeaderboard :contestId="contestId"/>
</div>
  `
}