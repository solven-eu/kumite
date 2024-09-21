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


## How to get a refresh_token

This is demonstrated by the `js` app on the `/html/me` route. The API call is essentially `GET /api/login/v1/oauth2/token?refresh_token=true`.

## How to authenticate my Robot

(We refer to the wording access_token and refresh_token. However, we do not follow formally the standard regarding these tokens. This would be done in a later iteration, as it would require to split the Resource Server (serving the business API given an access_token) and the Authentication Server (which would provide and receive refresh_token)).

1. Connect to the `js` app
2. Submit the form on `/html/me` to get a RefreshToken. This refreshToken is long-lived: it expires after 1 year.
3. Generate AccessToken while being authenticated with the refreshToken: `curl -H "Authorization: OAuth <REFRESH_TOKEN>" <HOST>/api/v1/oauth2/token?refresh_token=true`

Long-lived JWT shall later be banned based on their `jid`.