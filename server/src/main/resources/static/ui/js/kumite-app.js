import { watch } from "vue";
import { mapState } from "pinia";
import { useKumiteStore } from "./store.js";

export default {
	computed: {
		...mapState(useKumiteStore, [
			"account",
			"tokens",
			"nbAccountFetching",
			"playingPlayerId",
		]),
	},
	setup() {
		const store = useKumiteStore();

		// https://pinia.vuejs.org/core-concepts/state.html
		// Bottom of the page: there is a snippet for automatic persistence in localStorage
		// We still need to reload from localStorage on boot
		watch(
			store.$state,
			(state) => {
				// persist the whole state to the local storage whenever it changes
				localStorage.setItem("kumiteState", JSON.stringify(state));
			},
			{ deep: true },
		);

		store.loadUser();
		store.loadUserTokens();

		return {};
	},
	template: `
<div class="container">
  <nav class="navbar navbar-expand-lg navbar-light bg-light">
    <div class="container-fluid">
      <RouterLink class="navbar-brand" to="/">Kumite</RouterLink>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarSupportedContent">
        <ul class="navbar-nav me-auto mb-2 mb-lg-0">
          <li class="nav-item">
            <RouterLink class="nav-link active" aria-current="page" to="/">Home</RouterLink>
          </li>
          <li class="nav-item">
            <RouterLink class="nav-link" to="/html/games">Games</RouterLink>
          </li>
			<li class="nav-item">
			  <RouterLink class="nav-link" to="/html/contests">Contests</RouterLink>
			</li>
			  <li class="nav-item">
			    <RouterLink class="nav-link" to="/html/about">About</RouterLink>
			  </li>

			  <li class="nav-item" v-if="account.raw">
			    Current user: {{account.raw.name}}<img :src="account.raw.picture" class="img-thumbnail" alt="..." v-if="account.raw.picture">
			  </li>
        </ul>
      </div>
    </div>
  </nav>
  
  <main>
    <RouterView />
  </main>

  <p>
    <strong>Current route path:</strong> {{ $route.fullPath }}
  </p>
	<!--p>
	  <strong>Current account:</strong> {{ account }}
	</p-->
	  <p>
	    <strong>playerId:</strong> {{ playingPlayerId }}
	  </p>
		<!--p>
		  <strong>tokens:</strong> {{ tokens }}
		</p-->
  </div>
  `,
};
