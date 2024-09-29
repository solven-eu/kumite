import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	props: {
		gameId: {
			type: String,
			required: true,
		},
	},
	setup(props) {
		const store = useKumiteStore();

		return {};
	},
	template: /* HTML */ `
        <RouterLink :to="{path:'/html/games/' + gameId + '/contest-form'}"><i class="bi bi-node-plus"></i> Create your own contest</RouterLink>
    `,
};
