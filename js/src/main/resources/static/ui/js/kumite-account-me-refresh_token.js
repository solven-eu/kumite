import { ref } from "vue";

import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
import { Tooltip } from "bootstrap";

export default {
	components: {},
	setup(props) {
		const store = useKumiteStore();

		const refreshToken = ref("");

		const generateRefreshToken = function () {
			console.debug("Generating a refresh_token");
			async function fetchFromUrl(url) {
				try {
					const response = await fetch(url);
					if (!response.ok) {
						throw new Error("Rejected request for games url" + url);
					}

					const responseJson = await response.json();
					const refreshTokenWrapper = responseJson;

					console.info("refreshToken", refreshTokenWrapper);

					refreshToken.value = refreshTokenWrapper;
				} catch (e) {
					console.error("Issue on Network: ", e);
					exampleMovesMetadata.value.error = e;
				}
			}

			fetchFromUrl(`/api/login/v1/oauth2/token?refresh_token=true`);
		};

		// https://getbootstrap.com/docs/5.3/components/tooltips/
		// https://stackoverflow.com/questions/69053972/adding-bootstrap-5-tooltip-to-vue-3
		new Tooltip(document.body, { selector: "[data-bs-toggle='tooltip']" });

		// The update of the tooltip title through Vue does not actually update the text: hence we also update the button title
		const copyToClipboardTooltip = ref("Copy to clipboard");
		const copyToClipboardClass = ref("btn btn-primary");

		const copyRefreshTokenToClipboard = function () {
			// https://www.w3schools.com/howto/howto_js_copy_clipboard.asp

			// Copy the text inside the text field
			navigator.clipboard.writeText(refreshToken.value.refresh_token);

			// Alert the copied text
			console.log("refresh_token copied to clipboard");
			copyToClipboardTooltip.value = "Copied";
			copyToClipboardClass.value = "btn btn-success";
		};

		return { generateRefreshToken, refreshToken, copyRefreshTokenToClipboard, copyToClipboardTooltip, copyToClipboardClass };
	},
	template: /* HTML */ `
        <div v-if="!refreshToken">
            You want to develop your own long-running robot?
            <button type="button" @click="generateRefreshToken" class="btn btn-primary">Generate an refresh_token</button>
        </div>
        <div v-else>
            <form>
                <div class="input-group">
                    <span class="input-group-btn">
                        <!-- https://stackoverflow.com/questions/33584392/bootstraps-tooltip-doesnt-disappear-after-button-click-mouseleave -->
                        <button
                            :class="copyToClipboardClass"
                            type="button"
                            id="copy-button"
                            data-toggle="tooltip"
                            data-placement="button"
                            @click="copyRefreshTokenToClipboard"
                            data-bs-toggle="tooltip"
                            :data-bs-title="copyToClipboardTooltip"
                        >
                            <i class="bi bi-clipboard"></i>
                        </button>
                    </span>
                    Refresh_token:
                    <pre style="white-space: pre-wrap;" class="border"><code id="copy-input">{{refreshToken.refresh_token}}</code></pre>
                </div>
            </form>
            (Save it now on your side, as it is not saved in Kumite-Server)
        </div>
    `,
};
