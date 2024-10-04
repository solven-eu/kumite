import { ref, computed, watch } from "vue";

import { mapState } from "pinia";
import { useUserStore } from "./store-user.js";

import { Modal } from "bootstrap";

import LoginRef from "./login-ref.js";

export default {
	components: {
		LoginRef,
	},
	props: {
		logout: {
			type: String,
			required: false,
		},
	},
	computed: {
		...mapState(useUserStore, ["nbAccountFetching", "account", "isLoggedIn"]),
		...mapState(useUserStore, {
			user(store) {
				return store.account;
			},
		}),
	},
	setup(props) {
		const userStore = useUserStore();

		userStore.loadUser();

		watch(
			() => userStore.expectedToBeLoggedIn,
			(newValue, olValue) => {
				if (newValue && !olValue) {
					// Open the modal only when transitionning into expectedToBeLoggedIn
					console.log("expectedToBeLoggedIn turned to true. Opening the loginModal");

					// https://stackoverflow.com/questions/11404711/how-can-i-trigger-a-bootstrap-modal-programmatically
					// https://stackoverflow.com/questions/71432924/vuejs-3-and-bootstrap-5-modal-reusable-component-show-programmatically
					// https://getbootstrap.com/docs/5.0/components/modal/#via-javascript
					let loginModal = new Modal(document.getElementById("loginModal"), {});
					// https://getbootstrap.com/docs/5.0/components/modal/#show
					loginModal.show();
				}
			},
		);

		return {};
	},
	template: /* HTML */ `
        <div class="modal fade" id="loginModal" tabindex="-1" aria-labelledby="loginModalLabel" aria-hidden="true">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="loginModalLabel">Login</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <LoginRef data-bs-dismiss="modal" />
                    </div>
                </div>
            </div>
        </div>
    `,
};
