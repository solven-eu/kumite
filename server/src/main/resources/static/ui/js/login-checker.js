// my-component.js
import { ref } from 'vue'
import LoginOptions from './login-providers.js'
import KumiteGames from './kumite-games.js'

export default {
// https://vuejs.org/guide/components/registration#local-registration
components: {
  LoginOptions,
  KumiteGames
},
  setup() {
	const error = ref({});
	const isLoading = ref(true);
	const isAuthenticated = ref(false);
	const user = ref({});

	async function theData(url) {
	  try {
	    isLoading.value = true;
	    const response = await fetch(url);
//		if (!response.ok) {
//		  throw new Error(`Response status: ${response.status}`);
//		}
//	    const responseJson = {};; //;
	    isAuthenticated.value = response.status !== 401;
if (	isAuthenticated.value) {
	user.value = await response.json();
	console.log("User is authenticated", user.value);
} else {

	console.log("User is NOT authenticated");
}
	  } catch(e) {
	    console.error('Issue on Network: ', e)
		error.value = e;
	  } finally {
	    isLoading.value = false;
	  }
	};

	theData('/api/private/user')
	
    return { isLoading, isAuthenticated, user }
  },
  template: `
    <div v-if="isLoading">
    	Loading...
	</div>
    <div v-else>
	  	<div v-if="isAuthenticated">
	  		Welcome {{user.raw.name}}. ?Logout?
		</div>
	  	<div v-else>
			<LoginOptions/>
		</div>
  	</div>
	<KumiteGames />
  `
}