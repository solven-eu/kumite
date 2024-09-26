# kumite

This project support building a platform to enable Bots to play Games.

## Motivation

1. Some people have fun coding efficient algorithms
2. Other people have fun playing games (board-games and/or video-games)
3. The intersection are people having fun coding algorithms playing games
4. Kumite wants to offer a platform to enable Algorithms to complete against each-others.

## Quick-start

*Do I need to be an experience developer to code my bot?*
No, Kumite API is fairly simple to integrate. Even complex games can be implemented with simple strategies.

*How can I authenticate my bot?*
A bot is authenticated with a long-lived `refresh_token`, which can be generated on [<kumite>/html/me](https://kumite-dev-d2844865d26b.herokuapp.com/html/me)

*Where is the API documentation?*
API can be browsed on:
- [OpenAPI / Swagger](https://kumite-dev-d2844865d26b.herokuapp.com/swagger-ui.html)
- Example Java integration: [KumiteWebclientServer](https://github.com/solven-eu/kumite/blob/master/player/src/main/java/eu/solven/kumite/app/server/KumiteWebclientServer.java)

*Where is the Project documentation?*

- [MkDocs](https://solven-eu.github.io/kumite/)

### Related projects

People interested in Kumite may have found interest in:

- [AIArena](https://aiarena.net/) *The AI Arena ladder provides an environment where Scripted and Deep Learning AIs fight in Starcraft 2.*

## Integrate a Bot

The flow is essentially the following:

1. Login as a Human/User
2. Fetch an access_token for a Robot/Player
3. List interesting Games (a Game being a set of rules defining winning/losing/scoring conditions)
4. List interesting Contests (a contest being an instance of a Game, joinable by contenders)
5. Load the board and/or fetch examples/availables moves
6. Publish a Move (or wait until it's your turn)
7. Repeat `Load the board` and `Publish moves` until the Game is Over, or you are satisfied with your score

Client examples:

- [KumiteWebclientServer](https://github.com/solven-eu/kumite/blob/master/player/src/main/java/eu/solven/kumite/app/server/KumiteWebclientServer.java)

## Playing a Bot

Once the Gaming API is integrated, you'd like your Bot to play automatically. Each game should be played specifically to be played efficiently. However, simplest gaming-loops are common to multiple games as-long-as the game provides a useful set of `exampleMove` given current `boardView`.

We demonstrate such simple gaming-loop in [RandomGamingLogic](https://github.com/solven-eu/kumite/blob/master/player/src/main/java/eu/solven/kumite/app/player/RandomGamingLogic.java)

## Register an additional game

For now, games has be be hard-wired into the contest server. You can contribute games like:

- [TravellingSalesmanProblem](https://github.com/solven-eu/kumite/blob/master/server/src/main/java/eu/solven/kumite/game/optimization/tsp/TravellingSalesmanProblem.java)
- [TicTacToe](https://github.com/solven-eu/kumite/blob/master/server/src/main/java/eu/solven/kumite/game/opposition/tictactoe/TicTacToe.java)
- [Lag](https://github.com/solven-eu/kumite/blob/master/server/src/main/java/eu/solven/kumite/game/optimization/lag/Lag.java)

# Contributing

If you'd like to contribute to the project, see our [CONTRIBUTING.MD](https://github.com/solven-eu/kumite/blob/master/CONTRIBUTING.MD)
