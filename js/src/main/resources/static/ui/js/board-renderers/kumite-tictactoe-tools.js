import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Circle } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/circle.js";
import { Line } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/line.js";
import { Group } from "https://cdn.jsdelivr.net/npm/two.js/src/group.js";

// https://github.com/jonobr1/two.js/tree/main?tab=readme-ov-file#running-in-headless-environments
// We may want to generate board views with server-side rendering
// It may help picking dynamically a nice way to render a given board
export default {
	width() {
		//window.innerWidth;
		return 256;
	},
	height() {
		// window.innerHeight;
		return 256;
	},
	renderSupport(renderer) {
		const width = this.width();
		const height = this.height();

		// The size of the X/O symbol
		const r = 16;

		for (let i = 1; i < 3; i++) {
			const hLine = new Line(r, (height / 3) * i, width - r, (height / 3) * i);
			renderer.scene.add(hLine);

			const vLine = new Line((width / 3) * i, r, (width / 3) * i, height - r);
			renderer.scene.add(vLine);
		}
	},
	// position goes from 0 to 8
	renderSymbol(renderer, position, symbol) {
		const x = Math.floor(position / 3);
		const y = position % 3;
		const group = new Group();

		// The size of the X/O symbol
		const r = 16;
		const s = this.width() / 3 / 2;

		const px = x * (this.width() / 3) + s;
		const py = y * (this.height() / 3) + s;

		if (symbol === "X") {
			console.debug("Print X at ", position);
			const line1 = new Line(px - r, py - r, px + r, py + r);
			group.add(line1);
			const line2 = new Line(px + r, py - r, px - r, py + r);
			group.add(line2);
		} else if (symbol === "O") {
			console.debug("Print O at ", position);
			const circle = new Circle(px, py, r);
			group.add(circle);
		} else {
			// Nothing to render on not played positions
		}

		return group;
	},
	renderPositions(renderer, positions) {
		for (let i = 0; i < 9; i++) {
			const symbol = positions.charAt(i);

			const group = this.renderSymbol(renderer, i, symbol);

			renderer.scene.add(group);
		}
	},
	renderMove(renderer, move) {
		// `move.position` is 1-based
		const group = this.renderSymbol(renderer, move.position - 1, move.symbol);

		return group;
	},
};
