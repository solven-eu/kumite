export default {
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
	setup() {
		return {};
	},
	// https://stackoverflow.com/questions/7717929/how-do-i-get-pre-style-overflow-scroll-height-150px-to-display-at-parti
	template: /* HTML */ ` <pre style="height: 10pc; overflow-y: scroll;" class="border">{{rawMove}}</pre> `,
};
