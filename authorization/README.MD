# Integration example

## How to get a refresh_token

This is demonstrated by the `js` app on the `/html/me` route. The API call is essentially `GET /api/login/v1/oauth2/token?refresh_token=true`.

## How to authenticate my Robot

(We refer to the wording access_token and refresh_token. However, we do not follow formally the standard regarding these tokens. This would be done in a later iteration, as it would require to split the Resource Server (serving the business API given an access_token) and the Authentication Server (which would provide and receive refresh_token)).

1. Connect to the `js` app
2. Submit the form on `/html/me` to get a RefreshToken. This refreshToken is long-lived: it expires after 1 year.
3. Generate AccessToken while being authenticated with the refreshToken: `curl -H "Authorization: OAuth <REFRESH_TOKEN>" <HOST>/api/v1/oauth2/token?refresh_token=true`

Long-lived JWT shall later be banned based on their `jid`.

## refresh_token

A refresh_token is a JWS: it can be parsed especially for its `playerIds` string-list claim

A playerId can be seen as an OAuth2 clientId.

A refresh_token is attached to multiple playerIds (BEWARE this does not follow OAuth2). This enables a single refresh_token to manage multiple playerIds, hence a single Robot with a single refresh_token can play multiple strategies.

An access_token is attached to a single playerId.