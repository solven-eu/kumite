import { watch } from "vue";
import { useKumiteStore } from "./store.js";

export default {
	setup() {
		const store = useKumiteStore();

		watch(
			store.$state,
			(state) => {
				// persist the whole state to the local storage whenever it changes
				localStorage.setItem("kumiteState", JSON.stringify(state));
			},
			{ deep: true },
		);

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
            <RouterLink class="nav-link" to="/games">Games</RouterLink>
          </li>
			<li class="nav-item">
			  <RouterLink class="nav-link" to="/contests">Contests</RouterLink>
			</li>
			  <li class="nav-item">
			    <RouterLink class="nav-link" to="/about">About</RouterLink>
			  </li>
        </ul>
        <!--form class="d-flex">
          <input class="form-control me-2" type="search" placeholder="Search" aria-label="Search">
          <button class="btn btn-outline-success" type="submit">Search</button>
        </form-->
      </div>
    </div>
  </nav>
  
  <main>
    <RouterView />
  </main>

  <p>
    <strong>Current route path:</strong> {{ $route.fullPath }}
  </p>
  </div>
  `,
};
