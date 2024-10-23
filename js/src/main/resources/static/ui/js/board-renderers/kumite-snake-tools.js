import { Renderer } from "https://cdn.jsdelivr.net/npm/two.js/src/renderers/svg.js";
import { Line } from "https://cdn.jsdelivr.net/npm/two.js/src/shapes/line.js";
import { Text } from "https://cdn.jsdelivr.net/npm/two.js/src/text.js";
import { Group } from "https://cdn.jsdelivr.net/npm/two.js/src/group.js";

// The font styles to apply to
// all text in the scene.
const styles = {
  family: 'proxima-nova, sans-serif',
  size: 50,
  leading: 5,
  weight: 900,
};

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

		// The number of block per row/column
		const r = 8;

		for (let i = 1; i < 8; i++) {
			const hLine = new Line(0, (height / r) * i, width, (height / r) * i);
			renderer.scene.add(hLine);

            const vLine = new Line((width / r) * i, 0, (width / r) * i, height);
			renderer.scene.add(vLine);
		}
	},
	// position goes from 0 to 8
	renderSymbol(renderer, position, symbol) {
        // The number of block per row/column
        const r = 8;
        
		const x = position % r;
		const y = Math.floor(position / r);
		const group = new Group();

        const s = this.width() / 8 / 2;

		const px = x * (this.width() / r)  + s;
		const py = y * (this.height() / r) + s;

		if (symbol !== "_") {
			console.log("Print " , symbol, " at ", position);
			const text = new Text(symbol, px, py , styles);
			group.add(text);
		} else {
			// Nothing to render on not played positions
		}

		return group;
	},
	renderPositions(renderer, positions) {
        if (positions.length != 8*8) {
            throw new Error("Unexpected length: " + positions.length);
        }
        
		for (let i = 0; i < 8*8; i++) {
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
