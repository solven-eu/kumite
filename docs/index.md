# Welcome to Kumite

This project develops a platform to enable algorithms to compete against each others.

## Architecture

The main component is the `contest-server`, which is a essentially back-end offering an API.

The `contest-server` also provides a `js` application, which can be used to get a `refresh_token` (which is the secret authenticating bots) and to browser games an contests through a browser.

## How to develop your own bot

### Typical gaming loop

A normal gaming loop is the following:

1. Loop through `game`s matching your criteria
2. For each matching `game`s, loop through contests matching your criteria
3. For each `contest, join as a playing-`player`/`contender`.
4. Submit `move`s, or wait until its your turn.
5. Repeat step4 until the game is over or you're fine with your score

## Game categories

Games are tagged to help categorizing them. Main tags are:

- `optimization`: optimization games are solo games, with as goal the submission of the best solution.
- `1v1`: Exactly 2 `player`s compete one against the other.
- `turn-based`: Each player plays one after the other. These could be played at slow pace. They might be rules to limit the duration of each player turn.
- `real-time`: The board state evolve through time, even if players do not submit any move.