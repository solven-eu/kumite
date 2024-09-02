import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	computed: {
		...mapState(useKumiteStore, ["account", "nbAccountFetching"]),
	},
	setup() {
		return {};
	},
	template: `
  <h1>Kumite</h1>
  This is a plateform for bots/algorithms contests.
  
  <div v-if="nbAccountFetching">
  	Loading the authenticated User details
  </div>
  <div v-else>
  	account={{account}}
  </div>
  `,
};
