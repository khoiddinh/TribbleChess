package org.cis1200.chess.GUI;

/*
 * CIS 120 HW09 - TicTacToe Demo
 * (c) University of Pennsylvania
 * Created by Bayley Tuch, Sabrina Green, and Nicolas Corona in Fall 2020.
 */

import org.cis1200.chess.engine.ChessBoard;
import org.cis1200.chess.engine.ChessEngine;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.AffineTransformOp;
import java.awt.geom.AffineTransform;

import static org.cis1200.chess.engine.ChessBoard.EMPTY_SQUARE;

import org.cis1200.chess.engine.Move;
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
    public static final int SQUARE_LENGTH = 70; // 100 pixels
    public static final int BOARD_WIDTH = SQUARE_LENGTH * 8; // 100 pixels per square
    public static final int BOARD_HEIGHT = SQUARE_LENGTH * 8;

    public static final String WHITE_TO_MOVE_STATUS = "White to move";
    public static final String BLACK_TO_MOVE_STATUS = "Black to move";

    private static HashMap<Character, BufferedImage> pieceToImage;

    public static final Color COLORED_SQUARE_COLOR = new Color(111,143,114);
    public static final Color LIGHT_SQUARE_COLOR = new Color(173,189,143);
    private int posSelected;

    private boolean isAIPlayingBlack = true;
    private static final ChessEngine AI_ENGINE = new ChessEngine();

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

        pieceToImage = new HashMap<>(); // init hash map

        posSelected = -1; // init variable
        // initialize PIECE_TO_IMAGE
        try {
            // white pieces
            pieceToImage.put('K', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/wk.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('Q', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/wq.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('R', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/wr.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('B', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/wb.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('N', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/wn.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('P', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/wp.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));

            // black pieces
            pieceToImage.put('k', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/bk.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('q', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/bq.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('r', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/br.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('b', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/bb.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('n', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/bn.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));
            pieceToImage.put('p', scaleImage(ImageIO.read(new File(
                    "org/cis1200/chess/GUI/assets/bp.png")),
                    SQUARE_LENGTH, SQUARE_LENGTH));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        /*
         * Listens for mouseclicks. Updates the model, then updates the game
         * board based off of the updated model.
         */
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();
                int pos = getClickPos(p);
                if (posSelected == -1) {
                    return; // short circuit
                }
                int moveIndex = getValidMoveIndex(posSelected, pos);
                if (moveIndex != -1) {
                    ArrayList<Move> possibleNextMoves = board.getLegalPossibleMoves();
                    board.makeMove(possibleNextMoves.get(moveIndex));
                    System.out.println("Moves: " + possibleNextMoves);
                    System.out.println(Arrays.deepToString(board.getMovePairs()));
                    posSelected = -1;
                }
                // updates the model given the coordinates of the mouseclick
                updateStatus(); // updates the status JLabel
                repaint(); // repaints the game board

                SwingUtilities.invokeLater(() -> {
                    // if AI is playing get best move
                    if (isAIPlayingBlack && !board.isWhiteTurn()
                            && board.checkWinner(board.getLegalPossibleMoves()) == 0) {
                        long startTime = System.currentTimeMillis();
                        Move aiMove = AI_ENGINE.getBestMove(board);
                        long endTime = System.currentTimeMillis();
                        board.makeMove(aiMove);
                        System.out.println("Nodes Searched: " + AI_ENGINE.getNodesSearched());
                        System.out.println("Time Searched (ms): " + (endTime - startTime));
                        System.out.println("Nodes per Second: " +
                                (((float) AI_ENGINE.getNodesSearched())
                                        / ((float) (endTime - startTime))) * 1000.0);
                        System.out.println("Pruned: " + AI_ENGINE.getPruneAmount());
                        AI_ENGINE.resetNodeSearchCount();
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

                char[][] boardState = board.getBoardArray();
                if (boardState[row][col] != EMPTY_SQUARE && // only if non-empty
                        (board.isWhiteTurn()
                                && Character.isUpperCase(boardState[row][col]))
                        // and if white turn and select white piece
                        || (!board.isWhiteTurn()
                        && !Character.isUpperCase(boardState[row][col]))) {
                    // or if black turn and black piece
                    posSelected = pos;
                }
            }
        });
    }
    private static BufferedImage scaleImage(BufferedImage originalImage,
                                            int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height,
                originalImage.getType());
        AffineTransform at = AffineTransform.getScaleInstance(
                (double) width / originalImage.getWidth(),
                (double) height / originalImage.getHeight()
        );
        AffineTransformOp scaleOp = new AffineTransformOp(at,
                AffineTransformOp.TYPE_BILINEAR);
        return scaleOp.filter(originalImage, scaledImage);
    }

    // takes in position of click and returns the
    // pos of square clicked (top left index = 0)
    private int getClickPos(Point p) {
        int x = p.x;
        int y = p.y;

        int row = y / SQUARE_LENGTH;
        int col = x / SQUARE_LENGTH;

        return row * 8 + col;
    }

    // returns -1 if not valid move, else returns index of move
    private int getValidMoveIndex(int source, int target) {
        int[][] possibleNextMovePairs = board.getMovePairs();
        for (int i = 0; i < possibleNextMovePairs.length; i++) {
            int currSource = possibleNextMovePairs[i][0];
            int currTarget = possibleNextMovePairs[i][1];
            if (currSource == source && currTarget == target) {
                return i;
            }
        }
        return -1;
    }

    public boolean getIsAIPlayingBlack() {
        return isAIPlayingBlack;
    }

    public void toggleIsAIPlayingBlack() {
        isAIPlayingBlack = !isAIPlayingBlack;
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
    public void undo() {
        if (board.getMoveStack().isEmpty()) {
            return; // if empty do nothing
        }
        board.undoLastMove();
        if (board.isWhiteTurn()) {
            status.setText(WHITE_TO_MOVE_STATUS);
        } else {
            status.setText(BLACK_TO_MOVE_STATUS);
        }

        repaint();

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
        char[][] boardState = board.getBoardArray();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                char pieceAtPos = boardState[row][col];

                if ((row + col) % 2 == 1) { // even row odd col OR odd row even col
                    g.setColor(COLORED_SQUARE_COLOR);
                    g.fillRect(col * SQUARE_LENGTH, row * SQUARE_LENGTH,
                            SQUARE_LENGTH, SQUARE_LENGTH);
                } else {
                    g.setColor(LIGHT_SQUARE_COLOR);
                    g.fillRect(col * SQUARE_LENGTH, row * SQUARE_LENGTH,
                            SQUARE_LENGTH, SQUARE_LENGTH);
                }
                if (pieceAtPos != EMPTY_SQUARE) { // if piece at pos, draw it
                    g.drawImage(pieceToImage.get(pieceAtPos),
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
