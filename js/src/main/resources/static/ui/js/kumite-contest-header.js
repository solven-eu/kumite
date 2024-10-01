import { ref, onMounted, onUnmounted } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteAccountRef from "./kumite-account-ref.js";
import KumiteContestDelete from "./kumite-contest-delete.js";

export default {
	components: {
		KumiteAccountRef,
		KumiteContestDelete,
	},
	props: {
		contestId: {
			type: String,
			required: true,
		},
		gameId: {
			type: String,
			required: true,
		},
	},
	computed: {
		...mapState(useKumiteStore, ["nbGameFetching", "nbContestFetching", "isLoggedIn", "account"]),
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
		}),
	},
	setup(props) {
		const store = useKumiteStore();

		const shortPollContestDynamicInterval = ref(null);

		function clearShortPollContestDynamic() {
			if (shortPollContestDynamicInterval.value) {
				console.log("Cancelling setInterval shortPollContestDynamic");
				clearInterval(shortPollContestDynamicInterval.value);
				shortPollContestDynamicInterval.value = null;
			}
		}

		/*
		 * Polling the contest status every 5seconds.
		 * The output can be used to cancel the polling.
		 */
		function shortPollContestDynamic() {
			// Cancel any existing related setInterval
			clearShortPollContestDynamic();

			const intervalPeriodMs = 50000;
			console.log("setInterval", "shortPollContestDynamic", intervalPeriodMs);

			const nextInterval = setInterval(() => {
				console.log("Intervalled shortPollContestDynamic");
				store.loadContest(props.contestId, props.gameId);
			}, intervalPeriodMs);
			shortPollContestDynamicInterval.value = nextInterval;

			return nextInterval;
		}

		onMounted(() => {
			shortPollContestDynamic();
		});

		onUnmounted(() => {
			clearShortPollContestDynamic();
		});

		store.loadContestIfMissing(props.contestId, props.gameId);

		return {};
	},
	template: /* HTML */ `
        <div v-if="(!game || !contest) && (nbGameFetching > 0 || nbContestFetching > 0)">
            <div class="spinner-border" role="status">
                <span class="visually-hidden">Loading contestId={{contestId}}</span>
            </div>
        </div>
        <div v-else-if="game.error || contest.error">{{game.error || contest.error}}</div>
        <span v-else>
            <h2>
                <RouterLink :to="{path:'/html/games/' + gameId + '/contest/' + contestId}"
                    ><i class="bi bi-trophy"></i> {{contest.constantMetadata.name}}</RouterLink
                >
                <RouterLink :to="{path:'/html/games/' + gameId}"><i class="bi bi-arrow-90deg-left"></i></RouterLink>
            </h2>

            <ul>
                <li>author: <KumiteAccountRef :accountId="contest.constantMetadata.author" /></li>
                <li>created: {{contest.constantMetadata.created}}</li>
                <li v-if="isLoggedIn && contest.constantMetadata.author == account.accountId">
                    <KumiteContestDelete :gameId="gameId" :contestId="contestId" />
                </li>
                <li>
                    {{contest.dynamicMetadata.contenders.length}} contenders / {{ contest.constantMetadata.minPlayers }} required players / {{
                    contest.constantMetadata.maxPlayers }} maximum players
                </li>
            </ul>
        </span>
    `,
};
