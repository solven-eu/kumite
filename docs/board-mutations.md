# Board mutations

A board can mutate for various reasons:

- a contender joined the contest
- a contender played a move
- a time-frame happened (for `real-time` games)
- the contest is `gameover` (due to game rules, or an external reasons (e.g. some exception))

## Concurrency model

To guarantee consistency through board mutations, all mutations are done through a single thread. This is managed by `BoardLifecycleManager`. There may be multiple threads mutating boards, which is safe as long as given contestId is always managed by the same thread (e.g. through a hash `contestId => threadId`).

## Persistence of the board

The board state is split in multiple concepts/repositories:

- ContestConstantMetadata: these are defined when the contest is created and do not change through time
- IKumiteBoard: this is the custom board, as defined per the game. It holds the whole board state, and shall evolve only through `BoardLifecycleManager`.
- BoardDynamicMetadata: holds dynamic metadata about a board, without any board specificity. It includes the gameOver (especially if it has been forced out of the game rules).

## Events

To prevent the need to poll the contest game, nor to couple too many components together, we rely on an `EventBus` to notify about the contest lifecycle.

Events include:

- ContestIsCreated
- PlayerJoinedBoard
- PlayerCanMove: send when a player switched from `not being able to move` to `being able to me`.
- PlayerMoved
- ContestIsGameover

## BoardStateId

The boardStateId can be used to:

- know if current board is the latest board or not: it is useful not to have to fetch/analyze/poll the whole board.
