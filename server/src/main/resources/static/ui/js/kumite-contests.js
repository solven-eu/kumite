// my-component.js
import { ref } from 'vue'
export default {
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

	theData('/contests?game_id=' + props.game_id)
	
    return {isLoading, contests }
  },
  // https://vuejs.org/guide/components/props.html
  props: {
    game_id: String,
  },
  template: `
  <div v-if="isLoading">
  	Loading contests
	</div>
	<div v-else>
	  <li v-for="item in contests">
	    {{item}}
	  </li>
  </div>
  `
}