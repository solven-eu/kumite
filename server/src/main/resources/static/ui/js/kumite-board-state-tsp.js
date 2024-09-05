import { ref, onMounted } from "vue";

import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Circle } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/circle.js";

// https://github.com/jonobr1/two.js/tree/main?tab=readme-ov-file#running-in-headless-environments
// We may want to generate board views with server-side rendering
// It may help picking dynamically a nice way to render a given board
export default {
	// https://vuejs.org/guide/components/registration#local-registration
	components: {
		Renderer,
		Circle,
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

			board.cities.forEach((city) => {
				// `city.x` and `city.y` ranges in [0;1]
				const circle = new Circle(width * city.x, height * city.y, 1);
				renderer.scene.add(circle);

				renderer.render();
			});
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
