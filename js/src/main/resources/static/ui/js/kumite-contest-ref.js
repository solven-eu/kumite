import {} from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	components: {
	},
	props: {
		contestId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, {
			contest(store) {
				return store.contests[this.contestId] || { error: "not_loaded" };
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		store.loadContestIfMissing(props.contestId);

		return {};
	},
	template: /* HTML */ `

    <RouterLink :to="{path:'/html/games/' + contest.constantMetadata.gameId + '/contest/' + contestId}"
        >
        <i class="bi bi-trophy"></i> {{contest.constantMetadata.name}}
        </RouterLink
    >
    `,
};
