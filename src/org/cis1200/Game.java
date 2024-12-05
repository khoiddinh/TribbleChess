package org.cis1200;

import javax.swing.*;

// TODO:
/*
    reorganize files so that theres chess engine
    which has all the low level representation (for the ai)
    then the ChessBoard which abstracts it out to just the
    public facing getter methods
    ideally this will be stored in the 2d array form for ease of use
    also iterfaces with move pairs instead of bitboards
    still stores the bitboards so it can make the moves with the engine
*/

public class Game {
    /**
     * Main method run to start and run the game. Initializes the runnable game
     * class of your choosing and runs it. IMPORTANT: Do NOT delete! You MUST
     * include a main method in your final submission.
     */
    public static void main(String[] args) {
        // Set the game you want to run here
        Runnable game = new org.cis1200.chess.GUI.RunChess();

        SwingUtilities.invokeLater(game);
    }
}
