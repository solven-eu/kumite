import { ref, onMounted, watch } from "vue";

import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";

import TicTacToeTools from "./kumite-tictactoe-tools.js";

// https://github.com/jonobr1/two.js/tree/main?tab=readme-ov-file#running-in-headless-environments
// We may want to generate board views with server-side rendering
// It may help picking dynamically a nice way to render a given board
export default {
	components: {
		Renderer,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		board: {
			type: Object,
			required: true,
		},
	},
	setup(props) {
		const boardCanvas = ref(null);
		const board = props.board;

		const renderer = new Renderer({});
		function renderBoard() {
			const positions = board.positions;

			TicTacToeTools.renderSupport(renderer);
			TicTacToeTools.renderPositions(renderer, positions);

			renderer.render();
		}

		onMounted(() => {
			boardCanvas.value.appendChild(renderer.domElement);

			renderer.setSize(TicTacToeTools.width(), TicTacToeTools.height());

			renderBoard();

			watch(
				() => props.board,
				() => {
					renderBoard();
				},
				{ deep: true },
			);
		});

		return {
			boardCanvas,
		};
	},
	template: /* HTML */ `<div><div ref="boardCanvas" class="border" /></div>`,
};
