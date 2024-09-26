# API

The API can also be browsed through:

- [OpenAPI/Swagger](https://kumite-dev-d2844865d26b.herokuapp.com/swagger-ui.html)

## Login API

Given a `refresh_token` fetched manually on [<Kumite>/html/me](https://kumite-dev-d2844865d26b.herokuapp.com/html/me), one can:

- Create an `access_token` with `GET /api/v1/oauth2/token?player_id=somePlayerId`

A robot authenticates itself with an `access_token`. It is provided to the API with an `Authentication` header:

