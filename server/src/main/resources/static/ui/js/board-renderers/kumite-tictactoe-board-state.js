import { ref, onMounted } from "vue";

import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Circle } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/circle.js";
import { Line } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/line.js";
import { Group } from "https://cdn.jsdelivr.net/npm/two.js/src/group.js";

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
	},
	setup(props) {
		const boardCanvas = ref(null);
		const board = props.board;

		onMounted(() => {
			const width = 256; //window.innerWidth;
			const height = 256; // window.innerHeight;
			const renderer = new Renderer({});

			boardCanvas.value.appendChild(renderer.domElement);
			renderer.setSize(width, height);

			const positions = board.positions;

			for (let i = 0; i < 9; i++) {
				const char = positions.charAt(i);

				const x = i / 3;
				const y = i % 3;
				const group = new Group();

				if (char === "X") {
					const line = new Line(
						(width / 3) * x - 16,
						(height / 3) * y - 16,
						(width / 3) * x + 16,
						(height / 3) * y + 16,
					);
					group.add(line);
				} else if (char === "O") {
					const circle = new Circle(width * city.x, height * city.y, 1);
					group.add(circle);
				} else {
					return;
				}

				renderer.scene.add(group);
				renderer.render();
			}
		});

		return {
			boardCanvas,
		};
	},
	template: `
<div ref="boardCanvas" class="border">

</div>
  `,
};
