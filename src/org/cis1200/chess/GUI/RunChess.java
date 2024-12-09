package org.cis1200.chess.GUI;

import javax.swing.*;
import java.awt.*;

public class RunChess implements Runnable {
    public void run() {
        // NOTE: the 'final' keyword denotes immutability even for local variables.

        // Top-level frame in which game components live
        final JFrame frame = new JFrame("Chess");
        frame.setLocation(300, 300);

        // Status panel
        final JPanel status_panel = new JPanel();
        frame.add(status_panel, BorderLayout.SOUTH);
        final JLabel status = new JLabel("Setting up...");
        status_panel.add(status);

        // Game board
        final GameBoard board = new GameBoard(status);
        frame.add(board, BorderLayout.CENTER);

        // Reset button
        final JPanel control_panel = new JPanel();
        frame.add(control_panel, BorderLayout.NORTH);

        // Note here that when we add an action listener to the reset button, we
        // define it as an anonymous inner class that is an instance of
        // ActionListener with its actionPerformed() method overridden. When the
        // button is pressed, actionPerformed() will be called.
        final JButton reset = new JButton("Reset");
        reset.addActionListener(e -> board.reset());
        control_panel.add(reset);

        final JButton instructions = new JButton("Instructions/Description");
        instructions.addActionListener(e -> {
            // Display a simple instructions dialog
            JOptionPane.showMessageDialog(
                    frame,
                    "How to Play:\n\n" +
                            "1. Each type of piece moves in a distinct way.\n" +
                            "2. Pawns move forward one square at a time (with some exceptions).\n" +
                            "3. Rooks move horizontally or vertically any number of squares.\n" +
                            "4. Knights move in an L-shape.\n" +
                            "5. Bishops move diagonally any number of squares.\n" +
                            "6. The Queen can move horizontally, vertically, and diagonally.\n" +
                            "7. The King can move one square in any direction.\n" +
                            "8. To move a piece, click and drag it to the desired square. \n" +
                            "The goal is to checkmate your opponent’s King. \n\n" +
                            "Notable Features: \n" +
                            "The Black pieces are controlled by an AI. " +
                            "I worked really hard on that :) \n" +
                                    "I was able to do this by optimizing " +
                            "the chess move generation " +
                                    "logic by using integer bit math instead of 2D arrays. \n" +
                                    "This means that all the pieces are stored as integers " +
                                    "and simply operated on to generate the moves. \n" +
                                    "If you're interested, look through the source code. " +
                            "I think the math is really cool! \n"
                    ,
                    "Instructions & Description",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        control_panel.add(instructions);

        // AI toggle button
        final JButton aiToggle = new JButton("AI Playing: On");
        aiToggle.addActionListener(e -> {
            board.toggleIsAIPlayingBlack();  // toggle the state
            aiToggle.setText(board.getIsAIPlayingBlack()
                    ? "AI Playing: On" : "AI Playing: Off");
        });
        control_panel.add(aiToggle);

        final JButton undo = new JButton("Undo");
        undo.addActionListener(e -> {
            board.undo();
        });
        // control_panel.add(undo);

        // Put the frame on the screen
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Start the game
        board.reset();
    }
}
