import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

import KumiteJsonBoardState from "./board-renderers/kumite-json-board-state.js";

export default {
	components: {
		KumiteJsonBoardState,
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
		curlGetBoard() {
			return "curl " + window.location.protocol + "//" + window.location.host + "/api/v1" + "/board?contest_id=" + this.contestId;
		},
		...mapState(useKumiteStore, {
			game(store) {
				return store.games[this.gameId];
			},
			contest(store) {
				return store.contests[this.contestId];
			},
			board(store) {
				return store.contests[this.contestId]?.board;
			},
		}),
	},
	setup() {
		return {};
	},
	template: /* HTML */ `
        <!-- Button trigger modal -->
        <button type="button" class="btn btn-primary  btn-sm" data-bs-toggle="modal" data-bs-target="#exampleModal">cURL</button>

        <!-- Modal -->
        <div class="modal fade" id="bordViewCurl" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="exampleModalLabel">Fetch the board</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <!-- https://stackoverflow.com/questions/4611591/code-vs-pre-vs-samp-for-inline-and-block-code-snippets -->
                    <div class="modal-body">
                        <pre style="overflow-y: scroll;" class="border"><code>{{curlGetBoard}}</code></pre>
                        <KumiteJsonBoardState :board="board" />
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                    </div>
                </div>
            </div>
        </div>
    `,
};
