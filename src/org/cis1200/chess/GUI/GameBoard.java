package org.cis1200.chess.GUI;

/*
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

import org.cis1200.chess.engine.ChessBoard;
import org.cis1200.chess.engine.MoveGenerationPrecompute;
import org.cis1200.chess.engine.MoveGenerationPrecompute.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

import static org.cis1200.chess.engine.ChessBoard.EMPTY_SQUARE;

import org.cis1200.chess.engine.ChessEngine;
import org.cis1200.chess.engine.ChessEngine.*;
/**
 * This class instantiates a TicTacToe object, which is the model for the game.
 * As the user clicks the game board, the model is updated. Whenever the model
 * is updated, the game board repaints itself and updates its status JLabel to
 * reflect the current state of the model.
 * 
 * This game adheres to a Model-View-Controller design framework. This
 * framework is very effective for turn-based games. We STRONGLY
 * recommend you review these lecture slides, starting at slide 8,
 * for more details on Model-View-Controller:
 * https://www.seas.upenn.edu/~cis120/current/files/slides/lec37.pdf
 * 
 * In a Model-View-Controller framework, GameBoard stores the model as a field
 * and acts as both the controller (with a MouseListener) and the view (with
 * its paintComponent method and the status JLabel).
 */
@SuppressWarnings("serial")
public class GameBoard extends JPanel {

    private ChessBoard board; // model for the game
    private JLabel status; // current status text

    // Game constants
    public static final int SQUARE_LENGTH = 60; // 100 pixels
    public static final int BOARD_WIDTH = SQUARE_LENGTH*8; // 100 pixels per square
    public static final int BOARD_HEIGHT = SQUARE_LENGTH*8;

    public static final String WHITE_TO_MOVE_STATUS = "White to move";
    public static final String BLACK_TO_MOVE_STATUS = "Black to move";

    public static HashMap<Character, BufferedImage> PIECE_TO_IMAGE;

    public static final Color coloredSquareColor = new Color(160,82,45);

    private char[][] boardState;
    private int[][] possibleNextMovePairs; // pairs for checking if valid
    private ArrayList<Integer> possibleNextMoves; // to actually move
    private int posSelected;

    private static final boolean isAIPlayingBlack = true;
    private static final ChessEngine aiEngine = new ChessEngine();
    private ArrayList<Integer> moveList;
    /**
     * Initializes the game board.
     */
    public GameBoard(JLabel statusInit) {
        // creates border around the court area, JComponent method
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Enable keyboard focus on the court area. When this component has the
        // keyboard focus, key events are handled by its key listener.
        setFocusable(true);

        board = new ChessBoard(); // initializes model for the game
        status = statusInit; // initializes the status JLabel

        PIECE_TO_IMAGE = new HashMap<>(); // init hash map

        posSelected = -1; // init variable
        boardState = board.getBoardArray(); // init variable
        possibleNextMovePairs = board.getMovePairs(); // init variable
        possibleNextMoves = board.getLegalPossibleMoves();
        System.out.println(Arrays.deepToString(possibleNextMovePairs));

        // DEBUG
        moveList = new ArrayList<>();
        // initialize PIECE_TO_IMAGE
        try {
            // white pieces
            PIECE_TO_IMAGE.put('K', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_klt60.png")));
            PIECE_TO_IMAGE.put('Q', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_qlt60.png")));
            PIECE_TO_IMAGE.put('R', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_rlt60.png")));
            PIECE_TO_IMAGE.put('B', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_blt60.png")));
            PIECE_TO_IMAGE.put('N', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_nlt60.png")));
            PIECE_TO_IMAGE.put('P', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_plt60.png")));

            // black pieces
            PIECE_TO_IMAGE.put('k', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_kdt60.png")));
            PIECE_TO_IMAGE.put('q', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_qdt60.png")));
            PIECE_TO_IMAGE.put('r', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_rdt60.png")));
            PIECE_TO_IMAGE.put('b', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_bdt60.png")));
            PIECE_TO_IMAGE.put('n', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_ndt60.png")));
            PIECE_TO_IMAGE.put('p', ImageIO.read(new File("org/cis1200/chess/GUI/assets/Chess_pdt60.png")));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
         * Listens for mouseclicks. Updates the model, then updates the game
         * board based off of the updated model.
         */
        // TODO: fix mouse listeners
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();
                int pos = getClickPos(p);
                System.out.println(posSelected);
                System.out.println(pos);
                if (posSelected == -1) return; // short circuit
                int moveIndex = getValidMoveIndex(posSelected, pos);
                if (moveIndex != -1) {
                    board.makeMove(possibleNextMoves.get(moveIndex));
                    System.out.println("MOVE: " + possibleNextMoves.get(moveIndex));
                    System.out.println(board.visualizeBoard());
                    moveList.add(possibleNextMoves.get(moveIndex)); // TODO: DEBUG REMOVE
                    System.out.println(moveList.toString());
                    possibleNextMoves = board.getLegalPossibleMoves();
                    possibleNextMovePairs = board.getMovePairs();
                    System.out.println(Arrays.deepToString(possibleNextMovePairs));
                    boardState = board.getBoardArray();
                    posSelected = -1;
                }
                // updates the model given the coordinates of the mouseclick
                updateStatus(); // updates the status JLabel
                repaint(); // repaints the game board

                SwingUtilities.invokeLater(() -> {
                    // if AI is playing get best move
                    if (isAIPlayingBlack && !board.isWhiteTurn()) {
                        status.setText("AI is thinking...");
                        long startTime = System.currentTimeMillis();
                        int aiMove = aiEngine.getBestMove(board);
                        long endTime = System.currentTimeMillis();
                        board.printBinary(aiMove);
                        board.makeMove(aiMove);
                        moveList.add(aiMove);
                        possibleNextMoves = board.getLegalPossibleMoves();
                        possibleNextMovePairs = board.getMovePairs();
                        boardState = board.getBoardArray();
                        System.out.println("Nodes Searched: " + aiEngine.nodesSearched);
                        System.out.println("Time Searched (ms): " + (endTime-startTime));
                        System.out.println("Nodes per Second: " + (((float) aiEngine.nodesSearched) / ((float) (endTime-startTime))) * 1000.0);
                        System.out.println("Pruned: " + aiEngine.pruneAmount);
                        aiEngine.nodesSearched = 0;
                        // don't have to reset posSelected because I already did above
                    }
                    updateStatus();
                    repaint();
                });
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                Point p = e.getPoint();
                int pos = getClickPos(p);

                int row = pos / 8;
                int col = pos % 8;
                if (boardState[row][col] != EMPTY_SQUARE && // only if non-empty
                        (board.isWhiteTurn() && Character.isUpperCase(boardState[row][col])) // and if white turn and select white piece
                        || (!board.isWhiteTurn() && !Character.isUpperCase(boardState[row][col]))) { // or if black turn and black piece
                    posSelected = pos;
                }
            }
        });
    }

    // takes in position of click and returns the pos of square clicked (top left index = 0)
    private int getClickPos(Point p) {
        int x = p.x;
        int y = p.y;

        int row = y / SQUARE_LENGTH;
        int col = x / SQUARE_LENGTH;

        return row * 8 + col;
    }
    // TODO: optimize?
    // returns -1 if not valid move, else returns index of move
    private int getValidMoveIndex(int source, int target) {
        for (int i = 0; i < possibleNextMovePairs.length; i++) {
            int currSource = possibleNextMovePairs[i][0];
            int currTarget = possibleNextMovePairs[i][1];
            if (currSource == source && currTarget == target) {
                return i;
            }
        }
        return -1;
    }
    /**
     * (Re-)sets the game to its initial state.
     */
    public void reset() {
        board.reset();
        status.setText("White to Move");
        repaint();

        // Makes sure this component has keyboard/mouse focus
        requestFocusInWindow();
    }

    /**
     * Updates the JLabel to reflect the current state of the game.
     */
    private void updateStatus() {
        if (board.isWhiteTurn()) {
            status.setText(WHITE_TO_MOVE_STATUS);
        } else {
            status.setText(BLACK_TO_MOVE_STATUS);
        }

        int gameState = board.checkWinner(board.getLegalPossibleMoves());
        if (gameState == -1) {
            status.setText("Black Wins");
        } else if (gameState == 1) {
            status.setText("White Wins");
        } else if (gameState == 2) {
            status.setText("It's a tie.");
        }
    }

    /**
     * Draws the game board.
     * 
     * There are many ways to draw a game board. This approach
     * will not be sufficient for most games, because it is not
     * modular. All of the logic for drawing the game board is
     * in this method, and it does not take advantage of helper
     * methods. Consider breaking up your paintComponent logic
     * into multiple methods or classes, like Mushroom of Doom.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draws board grid
        int unitWidth = BOARD_WIDTH / 8;
        int unitHeight = BOARD_HEIGHT / 8;

        // draw horizontal lines
        for (int y = unitHeight; y < BOARD_HEIGHT; y += unitHeight) {
            g.drawLine(0, y, BOARD_WIDTH, y);
        }
        // draw vertical lines
        for (int x = unitWidth; x < BOARD_WIDTH; x += unitWidth) {
            g.drawLine(x, 0, x, BOARD_HEIGHT);
        }

        // Draw pieces and color squares
        System.out.println(Arrays.deepToString(boardState));

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char pieceAtPos = boardState[row][col];
                if (row % 2 == 1 || col % 2 == 1) { // if row or col are odd, color in
                    g.setColor(coloredSquareColor);
                    g.drawRect( col * SQUARE_LENGTH, row * SQUARE_LENGTH, SQUARE_LENGTH, SQUARE_LENGTH);
                    g.setColor(Color.BLACK);
                }
                if (pieceAtPos != EMPTY_SQUARE) { // if piece at pos, draw it
                    g.drawImage(PIECE_TO_IMAGE.get(pieceAtPos),
                            col * SQUARE_LENGTH, row * SQUARE_LENGTH,null);

                }
            }
        }
    }

    /**
     * Returns the size of the game board.
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(BOARD_WIDTH, BOARD_HEIGHT);
    }
}
