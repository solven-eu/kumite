import { ref, onMounted, watch } from "vue";

import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Circle } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/circle.js";
import { Line } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/line.js";

// Importing Two means importing all modules, without tree-shacking
// It is easier but (slightly) less efficient
import Two from "https://cdn.jsdelivr.net/npm/two.js/src/two.js";

// https://github.com/jonobr1/two.js/tree/main?tab=readme-ov-file#running-in-headless-environments
// We may want to generate board views with server-side rendering
// It may help picking dynamically a nice way to render a given board
export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		Renderer,
		Circle,
		Line,
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

		const width = 256; //window.innerWidth;
		const height = 256; // window.innerHeight;

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
		function renderMove(move) {
			if (!move || !move.cities) {
				console.log("Can not render invalid move", move);
				return;
			}

			console.log("Rendering move", move);

			const cityToPosition = {};

			board.cities.forEach((city) => {
				cityToPosition[city.name] = city;
			});

			move.cities.forEach((city, index) => {
				if (index >= 1) {
					const previousCity = cityToPosition[move.cities[index - 1]];
					if (!previousCity) {
						console.warn("Unknown city", move.cities[index - 1]);
						return;
					}
					const currentCity = cityToPosition[city];
					if (!currentCity) {
						console.warn("Unknown city", city);
						return;
					}

					const line = new Line(
						width * previousCity.x,
						height * previousCity.y,
						width * currentCity.x,
						height * currentCity.y,
					);
					renderer.scene.add(line);
				}
			});

			console.log("Rendering move", move);
			renderer.render();
		}

		onMounted(() => {
			console.log("onMounted", boardCanvas);
			boardCanvas.value.appendChild(renderer.domElement);
			renderer.setSize(width, height);

			board.cities.forEach((city) => {
				// `city.x` and `city.y` ranges in [0;1]
				const circle = new Circle(width * city.x, height * city.y, 1);
				renderer.scene.add(circle);
			});

			console.log("Rendering board", board);
			renderer.render();

			watch(
				() => props.rawMove,
				(newValue) => {
					renderRawMove();
				},
			);
		});

		return {
			boardCanvas,
			renderer,
		};
	},
	template: `
	<div ref="boardCanvas" class="border">
	</div>
	  `,
};
