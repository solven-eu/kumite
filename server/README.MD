# Security

We rely on a SecurityWebFilterChain:

- A first one, authenticating browser calls, given OAuth2 providers like Github
- A second one, authenticating API calls, given a JWT.

These are defined in [SocialWebFluxSecurity](https://github.com/search?q=repo%3Asolven-eu%2Fkumite%20SocialWebFluxSecurity&type=code).

## OAuth2 SecurityWebFilterChain

Typically, a User would browse to `/html/login` and pick an OAuth2 provider of its choice. Once this flow done, a cookie is saved for Kumite domain authenticating the user based on an external OAuth2 identity.

A web-call can be done to :

- `/api/login/v1/user` to fetch information about the session-authenticated user. This would return a 401 if not authenticated.
- `/api/login/v1/token` to fetch a short-lived access_token/JWT enabling API queries. This would return a 302 to `/html/login` if not authenticated.

## JWT SecurityWebFilterChain

Typically, a Robot would API-call to `/api/v1/...` with an `Authentication: Bearer someJwt`.

Such a JWT can be fetched:

- Short-lived (1h) (e.g. as done by the `js` application):
- Long-lived (1y) (e.g. as done by the `js` application):

Long-lived JWT shall later be banned based on their `jid`.