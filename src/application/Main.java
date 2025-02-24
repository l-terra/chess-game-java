package application;

import chess.ChessMatch;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ChessMatch chessMatch = new ChessMatch();

        while (true) {
            UI.printBoard(chessMatch.getPieces());
            System.out.println();
            System.out.print("Source position: ");
            ChessPosition source = UI.readChessPosition(sc);

            System.out.println();
            System.out.print("Next position:");
            System.out.println();
            ChessPosition next = UI.readChessPosition(sc);

            ChessPiece capturedPiece = chessMatch.performChessMove(source, next);
        }
    }
}
