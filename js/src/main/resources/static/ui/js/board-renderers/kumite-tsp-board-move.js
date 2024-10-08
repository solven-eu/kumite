import { ref, onMounted, watch } from "vue";

import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Circle } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/circle.js";
import { Line } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/line.js";
import { Group } from "https://cdn.jsdelivr.net/npm/two.js/src/group.js";

// Importing Two means importing all modules, without tree-shacking
// It is easier but (slightly) less efficient
// import Two from "https://cdn.jsdelivr.net/npm/two.js/src/two.js";

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

		const errorMessage = ref("");

		const width = 256; //window.innerWidth;
		const height = 256; // window.innerHeight;

		const renderer = new Renderer({});

		function renderRawMove() {
			let move;
			try {
				move = JSON.parse(props.rawMove);
			} catch (e) {
				console.warn("Invalid move", e);
				errorMessage.value = e.message;
				return;
			}

			renderMove(move);
		}

		const renderedMove = ref(null);

		function renderMove(move) {
			if (!move || !move.cities) {
				console.log("Can not render invalid move", move);
				errorMessage.value = "Invalid move";
				return;
			}

			console.log("Rendering move", move);

			const cityToPosition = {};

			board.cities.forEach((city) => {
				cityToPosition[city.name] = city;
			});

			const group = new Group();

			function checkAndAddLine(from, to) {
				if (!from) {
					console.warn("Unknown city", from);
					errorMessage.value = "Unknown city";
					return;
				}
				if (!to) {
					console.warn("Unknown city", to);
					errorMessage.value = "Unknown city";
					return;
				}

				const line = new Line(width * from.x, height * from.y, width * to.x, height * to.y);
				group.add(line);
			}

			move.cities.forEach((city, index) => {
				if (index == 0) {
					const previousCity = cityToPosition[move.cities[move.cities.length - 1]];
					const currentCity = cityToPosition[city];
					checkAndAddLine(previousCity, currentCity);
				} else {
					const previousCity = cityToPosition[move.cities[index - 1]];
					const currentCity = cityToPosition[city];

					checkAndAddLine(previousCity, currentCity);
				}
			});

			if (renderedMove.value) {
				// Undraw the previously rendered solution
				renderedMove.value.remove();
			}
			renderedMove.value = group;
			renderer.scene.add(group);

			console.log("Rendering move", move);
			renderer.render();
			errorMessage.value = "";
		}

		onMounted(() => {
			console.log("onMounted", boardCanvas);
			boardCanvas.value.appendChild(renderer.domElement);
			renderer.setSize(width, height);

			// Print the state
			console.log("Rendering board", board);
			board.cities.forEach((city) => {
				// `city.x` and `city.y` ranges in [0;1]
				const circle = new Circle(width * city.x, height * city.y, 1);
				renderer.scene.add(circle);
			});
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
			errorMessage,
		};
	},
	template: /* HTML */ `
        <div>
            <div ref="boardCanvas" class="border" />
            {{errorMessage}}
        </div>
    `,
};
