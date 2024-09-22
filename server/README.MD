# Security

We rely on a SecurityWebFilterChain:

- A first one, authenticating browser calls, given OAuth2 providers like Github
- A second one, authenticating API calls, given a JWT.

These are defined in [SocialWebFluxSecurity](https://github.com/search?q=repo%3Asolven-eu%2Fkumite%20SocialWebFluxSecurity&type=code).

## OAuth2 SecurityWebFilterChain

Typically, a User would browse to `/html/login` and pick an OAuth2 provider of its choice. Once this flow done, a cookie is saved for Kumite domain authenticating the user based on an external OAuth2 identity.

A web-call can be done to :

- `/api/login/v1/user` to fetch information about the session-authenticated user. This would return a 401 if not authenticated.
- `/api/login/v1/oauth2/token` to fetch a short-lived access_token/JWT enabling API queries. This would return a 302 to `/html/login` if not authenticated.

## JWT SecurityWebFilterChain

Typically, a Robot would API-call to `/api/v1/...` with an `Authentication: Bearer someJwt`.

Such a JWT can be fetched:

- Given an OAuth2 user (e.g. as done by the `js` application, on the `/api/login/v1/token` route):
- Given a RefreshToken (e.g. as done by the `player` application, on the `/api/v1/token` route):

In both cases, the JWT behave as a short-lived access_token: it expires after 1 hour.


## How to get a refresh_token and authenticate my Robot

see [authorization](../authorization/README.MD)