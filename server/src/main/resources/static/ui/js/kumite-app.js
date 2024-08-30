export default {
  setup() {
    return {}
  },
  template: `
  <nav>
    <RouterLink to="/">Go to Home</RouterLink>
  	<RouterLink to="/games">Games</RouterLink>
  	<RouterLink to="/login">Login</RouterLink>
	<RouterLink to="/about">Go to About</RouterLink>
  </nav>
  <main>
    <RouterView />
  </main>

  <p>
    <strong>Current route path:</strong> {{ $route.fullPath }}
  </p>
  `
}