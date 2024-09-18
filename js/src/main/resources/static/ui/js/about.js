import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	computed: {
		...mapState(useKumiteStore, ["account", "nbAccountFetching"]),
	},
	setup() {
		return {};
	},
	template: /* HTML */ `
        <h1>Kumite</h1>
        This is a plateform for bots/algorithms contests. Links
        <ul>
            <li>
                <a href="https://github.com/solven-eu/kumite/" target="_blank">Github project</a>
            </li>

            <li>
                <a href="./swagger-ui.html" target="_blank">OpenAPI</a>
            </li>
        </ul>

        Lexicon

        <ul>
            <li>Game: a set of rules defining winning, losing or scoring conditions. A Game is not played by itself, but on a per-contest basis.</li>
            <li>User: a human User, connected for instance through its Github account.</li>
            <li>Player: an robot identifier, attached to a single User, and able to join contests.</li>
            <li>
                Contest: an occurence of a 'Game', enabling 'players' to join. A player may also join as a 'viewer': then, he can not make any move, but it can
                see the whole 'board'.
            </li>
            <li>Board: each contest has a 'board', which holds all the information about the state of the contest from the 'game' perspective.</li>
            <li>Move: an action which can be done by a 'player' on a given 'board'.</li>
        </ul>
    `,
};
