# API

The API can also be browsed through:

- [OpenAPI/Swagger](https://kumite-dev-d2844865d26b.herokuapp.com/swagger-ui.html)
- [Java Webflux Example](https://github.com/solven-eu/kumite/blob/master/player/src/main/java/eu/solven/kumite/app/server/KumiteWebclientServer.java)

Here, we present an overview of the main entry-points.

## Login API

Given a `refresh_token` fetched manually on [<Kumite>/html/me](https://kumite-dev-d2844865d26b.herokuapp.com/html/me), one can:

- Create an `access_token` with `GET /api/v1/oauth2/token?player_id=somePlayerId`

A robot authenticates itself with an `access_token`. It is provided to the API with an `Authentication` header:

## Browsing API

Given an `access_token` fetched with the Login API, one can:

- Search `game`s with `GET /api/v1/games?tag=optimization`.
- Search `contest`s with `GET /api/v1/contests?accept_players=true`.

## Contender API

Once you found a relevant contest, once can:

- Preview the `board` of the `contest` with `GET /api/v1/board?contest_id=234`
- Join the `contest` as contender with `POST /api/v1/board/player?contest_id=234&player_id=123`
- Load the `board` of the `contest` as given `player` with `GET /api/v1/board?contest_id=234&player_id=123`
- Play a `move` with `POST /api/v1/board/move?contest_id=234&player_id=123`