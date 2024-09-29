import { ref, onMounted, watch } from "vue";

import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Circle } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/circle.js";
import { Line } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/line.js";
import { Group } from "https://cdn.jsdelivr.net/npm/two.js/src/group.js";

// Importing Two means importing all modules, without tree-shacking
// It is easier but (slightly) less efficient
// import Two from "https://cdn.jsdelivr.net/npm/two.js/src/two.js";

import TicTacToeTools from "./kumite-tictactoe-tools.js";

// https://github.com/jonobr1/two.js/tree/main?tab=readme-ov-file#running-in-headless-environments
// We may want to generate board views with server-side rendering
// It may help picking dynamically a nice way to render a given board
export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		Renderer,
		Circle,
		Line,
		Group,
	},
	// https://vuejs.org/guide/components/props.html
	props: {
		board: {
			type: Object,
			required: true,
		},
		rawMove: {
			type: String,
			required: true,
		},
	},
	setup(props) {
		const boardCanvas = ref(null);
		const board = props.board;

		const renderer = new Renderer({});

		function renderRawMove() {
			let move;
			try {
				move = JSON.parse(props.rawMove);
			} catch (e) {
				console.error("Invalid move", e);
				return;
			}

			renderMove(move);
		}

		const renderedGroup = ref(null);

		function renderMove(move) {
			if (!move || !move.position) {
				console.log("Can not render invalid move", move);
				return;
			}

			const group = TicTacToeTools.renderMove(renderer, move);

			if (renderedGroup.value) {
				// Undraw the previously rendered solution
				renderedGroup.value.remove();
			}
			renderedGroup.value = group;
			renderer.scene.add(group);

			console.debug("Rendering move", move);
			renderer.render();
		}

		onMounted(() => {
			boardCanvas.value.appendChild(renderer.domElement);

			renderer.setSize(TicTacToeTools.width(), TicTacToeTools.height());

			console.debug("Rendering board", board);
			{
				const positions = board.positions;

				TicTacToeTools.renderSupport(renderer);

				TicTacToeTools.renderPositions(renderer, positions);
			}
			renderer.render();

			watch(
				() => props.rawMove,
				() => {
					renderRawMove();
				},
			);
		});

		return {
			boardCanvas,
			renderer,
		};
	},
	template: /* HTML */ ` <div ref="boardCanvas" class="border" /> `,
};
