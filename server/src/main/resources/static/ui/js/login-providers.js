// my-component.js
import { ref } from "vue";
export default {
	setup() {
		const error = ref({});
		const isLoading = ref(true);
		const loginProviders = ref({
			loginProviders: [],
		});

		async function theData(url) {
			try {
				isLoading.value = true;
				const response = await fetch(url);
				const responseJson = await response.json();
				loginProviders.value = responseJson.list;
			} catch (e) {
				console.error("Issue on Network: ", e);
				error.value = e;
			} finally {
				isLoading.value = false;
			}
		}

		theData("http://localhost:8080/login/providers");

		return { isLoading, loginProviders };
	},
	template: /* HTML */ `
        <div v-if="isLoading">Loading login options</div>
        <div v-else>
            <li v-for="item in loginProviders">
                <a :href="item.login_url">{{ item.registration_id }}</a>
            </li>
        </div>
    `,
};
