# Authentication

## access_token usage

A robot authenticates itself with an `access_token`. It is provided to the API with an `Authentication` header:

```
curl -H "Authorization: OAuth <ACCESS_TOKEN>" 
    <HOST>/api/v1/games
```

`access-token`s are:
- short-lived: they generally expires after 1 hour.
- attached to a single playerId (i.e. the playerId can be seen as a `clientId`).

## access_token generation

An access_token is generated given a `refresh_token`

```
curl -H "Authorization: OAuth <REFRESH_TOKEN>" 
    <HOST>/api/v1/oauth2/token?refresh_token=true
```

## How to get a `refresh_token`

A `refresh_token` has to be fetched manually:

1. Connect to [<Kumite>](https://kumite-dev-d2844865d26b.herokuapp.com)
2. Submit the form on [<Kumite>/html/me](https://kumite-dev-d2844865d26b.herokuapp.com/html/me). This `refresh_token` is long-lived: it expires after 1 year.
3. Store it right away in a safe place, for instance as environment variable.

`refresh_token`s are:
- long-lived: they are valid for 1 year after their generation
- can be associated to 1/many/all playerIds.
- JWS: they can be open to work on their claims.
- can be banned individually given their `jti` claim.

### Is it awkward that a `refresh_token` manages multiple `playerId`s?

- From the `access-token` perspective, the `playerId` can be interpreted as a `clientId`.
- But a `refresh_token` can be attached to multiple playerIds.
- The goal if this design is to make it easier to manage multiple players/strategies through a single  secret/`refresh_token`.

## Which playerIds can be played given a `refresh_token`

The `playerIds` claim has type string-list. It holds the list of playerId playable by given `refresh_token`.

You can generate a `refresh_token` for any given set of playerIds at generation time.

BEWARE: we shall provide a way for a `refresh_token` to be valid for all playerIds, even those not created yet.

## Parallel with OAuth2 protocol

We refer to the wording `access_token` and `refresh_token`. However, we do not follow strictly OAuth2 regarding these tokens. This would be done in a later iteration, as it would require to split the Resource Server (serving the business API given an access_token) and the Authentication Server (which would provide and receive refresh_token).


