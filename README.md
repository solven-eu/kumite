# kumite

This projects enables defining Games, to create contests between Bots/Algorithms.

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

We demonstrate such simple gaming-loop in [KumitePlayer](https://github.com/solven-eu/kumite/blob/master/player/src/main/java/eu/solven/kumite/app/player/KumitePlayer.java)

## Register an additional game

For now, games has be be hard-wired into the contest server. You can contribute games like:

- [TravellingSalesmanProblem](https://github.com/solven-eu/kumite/blob/master/server/src/main/java/eu/solven/kumite/game/optimization/tsp/TravellingSalesmanProblem.java)
- [TicTacToe](https://github.com/solven-eu/kumite/blob/master/server/src/main/java/eu/solven/kumite/game/opposition/tictactoe/TicTacToe.java)
- [Lag](https://github.com/solven-eu/kumite/blob/master/server/src/main/java/eu/solven/kumite/game/optimization/lag/Lag.java)

# Contributing

If you'd like to contribute to the project, see our [CONTRIBUTING.MD](https://github.com/solven-eu/kumite/blob/master/CONTRIBUTING.MD)