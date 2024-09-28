# Lexicon

## Game

A game is a set of rules defining winning, losing or scoring conditions. A Game is not played by itself, but on a per-contest basis.

## User

A user is a human User, connected for instance through its Github account.

It is identified by the identity referential (e.g. `oauth2.providerId+sub`).

(It is unclear how we would manage joined-users, a.k.a. same human connecting through different identity referentials (e.g. Github and Google, with a common email address)).

## Account

The set of resources referring by an `accountId`.

It is identified by an `UUID`.

## Player

A robot identifier, attached to a `account`, and able to join `contest`s.

It is identified by an `UUID`.

##Contest

A contest is an instance of a `Game`. It can be joined by playing `player`s (also referred to as `contender`s) and viewing `player`s.

- A `contender` can play `move`s, following the `game` rules.
- A `viewer` can not play any `move`, but he can see the whole board, independently of any `fog-of-war`.

It is identified by an `UUID`.

## Board

Each `contest` has a `board`, which holds all the information about the state of the contest from the `game` perspective.

## Move

A `move` is an action which can be done by a `player` on a given `board`.

## Fog-of-War

Some `game` have imperfect information: each `player` can only see the fraction of the `board`. In some such games, a `contender` can only see the `board` around its own unit ; the `fog-of-war` is the mechanism preventing a `contender` to see the board out of its units range-of-vision.

## Gameover

A `contest` is `gameover` once its board state is final. No player will be able to submit a move ever.