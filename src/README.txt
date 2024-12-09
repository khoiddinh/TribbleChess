=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=
CIS 1200 Game Project README
PennKey: _______
=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=:=

===================
=: Core Concepts :=
===================

- List the four core concepts, the features they implement, and why each feature
  is an appropriate use of the concept. Incorporate the feedback you got after
  submitting your proposal.

  1. Collections and/or Maps:
  I leverage Collections significantly in my game in order to optimize for O(1) lookup times.
  For instance, in order to make and undo moves in my chess board, I need to use a Stack
  in order to add and pop moves efficiently (for the AI to search through enough nodes quickly).
  A stack is the best data structure for storing
  moves since it has O(1) lookup (peek), O(1) remove, and O(1) add. Additionally, the
  stack's FIFO (first in first out) functionality matches the way I need to pop the most
  recent move to undo it (there is unlimited undo). This is a key functionality because
  in order to test if moves are legal I need to play them and check if the king is in check.

  2. JUnit Testable Component:
  I use the MVC structure to make my board functionality unit testable without the GUI.
  This is done in both ChessBoardTest and MoveGenerationPrecomputationTest, which test
  the board move generation and playing functionality and the accuracy of the
  precomputation tables respectively. I was able to test important edge cases
  (i.e. king can't move into check, pinned pieces can't move).

  3. Complex Game Logic:
  All the rules of chess are implemented and tested in my game. This includes
  checks, king can't move into check, and pinned pieces. This means that the
  only moves that the chess engine will return are the ones that satisfy all of
  these contraints. Additionally, the checkmate works correctly and displays
  the winner in the status bar at the bottom of the GUI. The castle feature also works
  and includes the cases where moving a rook disables castle on that side only,
  and moving the king disables castle altogether. Castling also can't move if
  the spaces in between the king and the rook are attacked by an enemy piece.
  Finally, enPassant is implemented correctly by checking the move stack to
  ensure that the previous move allows for an enPassant during the current turn.

  4. Recursion:
  My AI uses negamax, which is a variation of minimax in order to run DFS and check
  all positions to a certain depth. It also alpha-beta pruning, which removes moves which
  are most likely not going to be relevant to the search since an ideal opponent would
  never pick that move. Due to using bitboards (64-bit signed integers) as my board representation
  as opposed to a 2D array, which reduces the amount of assembly instructions the code compiles to,
  my AI is able to search an average of 400 thousand positions per second
  (evaluates up to 4 million positions per move usually).
  I also applied a position and piece based heuristic which also depends on the stage of
  the game. For instance, it uses an MVV-LVA scoring algorithm to sort the moves to
  reduce the search space of the capture moves since it prioritizes captures by lower
  valued pieces first. It also has tables of where each piece is the strongest depending
  on the stage of the game which I took from the internet and modified based on my testing.

===============================
=: File Structure Screenshot :=
===============================
- Include a screenshot of your project's file structure. This should include
  all of the files in your project, and the folders they are in. You can
  upload this screenshot in your homework submission to gradescope, named 
  "file_structure.png".

=========================
=: Your Implementation :=
=========================

- Provide an overview of each of the classes in your code, and what their
  function is in the overall game.


  My main classes are ChessBoard, MoveGenerationPrecomputation, and Chess Engine.
  The more minor classes are AIEvaluation, BitBoardFunctions, CastleState, and Move.
  These minor classes mainly encapsulate information in an easier way so I can use
  them in the actual game.

  MoveGenerationPrecomputation: This class builds all the lookup tables (mostly 2D arrays)
  that ChessBoard uses in order to find all the possible moves and see who has won. By
  precomputing these values, I was able to make the code over 100 times faster since
  everything was O(1) lookup. It stores all the tables for where all the stepping pieces
  (king, knight, pawn) can go which ChessBoard accesses to apply it to the specific chess board.
  The sliding pieces use a technique called magic bitboards, which allows me to precompute
  every single possible move state for rooks, bishops, and queens with every combination of
  pieces in the way. This is done using a custom hashing function that I wrote so that
  each position could perfectly hash to its corresponding move possibilities, which would
  not be possible if I simply stored the board state due to size constraints (terabytes vs kilobytes).

  ChessBoard: This class stores the actual chess board state, and also has the high level
  functions which are called by the GUI. For example, the functions to get all the legal
  moves and check if someone has won is all in ChessBoard. It handles all the complex game
  logic functionality such as enPassant, castle, check, and pins.

  ChessEngine: This class contains the AI that runs the negamax evaluation of a board
  position that is given to it. All the heuristics and AI related search helper functions
  are contained here as well.

  AIEvaluation: This class simply stores the results of the
  AI evaluation (the move and score that the AI gives at the position) in one class.
  This allows me to bundle it effectively and is used in ChessEngine during the DFS search.

  BitBoardFunctions: This class is composed of static functions that are used as
  helper functions by the main classes. It mostly performs bitwise operations
  (AND, XOR, OR) in order to help with the move generation logic.

  CastleState: This class encapsulates the castle state for a specific game state.
  For example, it stores if white can castle right, left, or neither.
  It stores the same for black as well.

  Move: This class encapsulates the different fields of the move to
  play the move successfully. For instance, it stores the position, piece,
  whether it's a castle or enPassant move (not all fields listed for brevity).

- Were there any significant stumbling blocks while you were implementing your
  game (related to your design, or otherwise)?

  I think the big stumbling block while implementing my game was optimizing the speed
  of the move generation as much as possible. This is because having an AI requires an
  engine which can analyse millions of moves in a short amount of time.
  Chess is such a complex games with so many nuanced rules (pins, enPassant, promotion)
  which are very difficult to implement. At first, I tried using 2D arrays,
  but these were way too slow, so I switched to using 64-bit integers (long type).
  These are much faster because it compiles to fewer assembly instructions
  (AND, XOR, OR) integer operations run natively on x64 architectures,
  as opposed to arrays which have a lot of abstraction layers. It got to the point
  where I had to optimize the number of array lookups because even though they
  were O(1) time, they still slowed down the code a lot.

- Evaluate your design. Is there a good separation of functionality? How well is
  private state encapsulated? What would you refactor, if given the chance?

  The separation of functionality is good, and I was able to encapsulate different
  information such Move and CastleState which made the code more readable. Also,
  the private states are encapsulated in a way that the code can ensure that there is
  only a specific set of public functions that the GUI can query from which prevents
  the GUI from invalidly modifying or accessing fields that it shouldn't be. I don't see
  many things that I would refactor, but maybe I would break big functions like possibleMoves
  into more helper functions to increase readability.



========================
=: External Resources :=
========================

- Cite any external resources (images, tutorials, etc.) that you may have used 
  while implementing your game.

  I was inspired by these tables: https://www.chessprogramming.org/Simplified_Evaluation_Function
  for my static evaluation function
  I took the chess pieces from chess.com: https://www.chess.com/home
  I based my magic bitboard hashing algorithm on this (I built a modified version):
  https://www.chessprogramming.org/Magic_Bitboards
  I based my negamax algorithm off of this: https://www.chessprogramming.org/Negamax
  My alpha-beta pruning was based off of this: https://www.geeksforgeeks.org/
  minimax-algorithm-in-game-theory-set-4-alpha-beta-pruning/

