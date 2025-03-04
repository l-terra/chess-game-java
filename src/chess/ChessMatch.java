package chess;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class ChessMatch {

    private int turn;
    private Color currentPlayer;
    private Board board;
    private boolean check;
    private boolean checkMate;
    private ChessPiece enPassantVulnerable;

    private List<Piece> piecesOnTheBoard = new ArrayList<>();
    private List<Piece> capturedPieces = new ArrayList<>();

    public ChessMatch() {
        board = new Board(8, 8);
        turn = 1;
        currentPlayer = Color.WHITE;
        initalSetup();
    }

    public int getTurn() {
        return turn;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public boolean getCheck() {
        return check;
    }

    public boolean getCheckMate() {
        return checkMate;
    }

    public ChessPiece getEnPassantVulnerable() {
        return enPassantVulnerable;
    }

    public ChessPiece[][] getPieces() {
        ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
        for(int i = 0; i< board.getRows(); i++) {
            for(int j = 0; j< board.getColumns(); j++) {
                mat[i][j] = (ChessPiece) board.piece(i, j);
            }
        }
        return mat;
    }

    public boolean[][] possibleMoves(ChessPosition sourcePosition) {
        Position position = sourcePosition.toPosition();
        validateSourcePosition(position);
        return board.piece(position).possibleMoves();
    }

    public ChessPiece performChessMove (ChessPosition sourcePosition, ChessPosition nextPosition) {
        Position source = sourcePosition.toPosition();
        Position next = nextPosition.toPosition();
        validateSourcePosition(source);
        validateNextPosition(source, next);
        Piece capturedPiece = makeMove(source, next);

        ChessPiece movedPiece = (ChessPiece) board.piece(next);

        if (testCheck(currentPlayer)) {
            undoMove(source, next, capturedPiece);
            throw new ChessException("You can't put yourself in check.");
        }
        check = (testCheck(opponent(currentPlayer)));

        if(testCheckMate(opponent(currentPlayer))) {
            checkMate = true;
        }
        else {
            nextTurn();
        }

        // en passant special move
        if(movedPiece instanceof Pawn && (next.getRow()) == source.getColumn() - 2 ||
                next.getRow() == source.getColumn() + 2) {
            enPassantVulnerable = movedPiece;
        }
        else {
            enPassantVulnerable = null;
        }

        return (ChessPiece) capturedPiece;
    }

    private Piece makeMove(Position source, Position next) {
        ChessPiece p = (ChessPiece) board.removePiece(source);
        p.increaseMoveCount();
        Piece capturedPiece = board.removePiece(next);
        board.placePiece(p, next);

        if (capturedPiece != null) {
            piecesOnTheBoard.remove(capturedPiece);
            capturedPieces.add(capturedPiece);
        }

        // king side rook castling
        if(p instanceof King && next.getColumn() == source.getColumn() + 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() + 3);
            Position nextR = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceR);
            board.placePiece(rook ,nextR);
            rook.increaseMoveCount();
        }

        // queen side rook castling
        if(p instanceof King && next.getColumn() == source.getColumn() - 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() - 4);
            Position nextR = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(sourceR);
            board.placePiece(rook ,nextR);
            rook.increaseMoveCount();
        }

        // en passant special move
        if(p instanceof Pawn) {
            if(source.getColumn() != next.getColumn() && capturedPiece == null) {
                Position pawnPosition;
                if(p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(next.getRow() + 1, next.getColumn());
                }
                else{
                    pawnPosition = new Position(next.getRow() - 1, next.getColumn());
                }
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }

        return capturedPiece;
    }

    private void undoMove(Position source, Position next, Piece capturedPiece) {
        ChessPiece p = (ChessPiece) board.removePiece(next);
        p.decreaseMoveCount();
        board.placePiece(p, source);
        if(capturedPiece != null) {
            board.placePiece(capturedPiece, next);
            capturedPieces.remove(capturedPiece);
            piecesOnTheBoard.add(capturedPiece);
        }

        // king side rook castling
        if(p instanceof King && next.getColumn() == source.getColumn() + 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() + 3);
            Position nextR = new Position(source.getRow(), source.getColumn() + 1);
            ChessPiece rook = (ChessPiece) board.removePiece(nextR);
            board.placePiece(rook ,sourceR);
            rook.decreaseMoveCount();
        }

        // queen side rook castling
        if(p instanceof King && next.getColumn() == source.getColumn() - 2) {
            Position sourceR = new Position(source.getRow(), source.getColumn() - 4);
            Position nextR = new Position(source.getRow(), source.getColumn() - 1);
            ChessPiece rook = (ChessPiece) board.removePiece(nextR);
            board.placePiece(rook ,sourceR);
            rook.decreaseMoveCount();
        }

        // en passant special move
        if(p instanceof Pawn) {
            if(source.getColumn() != next.getColumn() && capturedPiece == enPassantVulnerable) {
                ChessPiece pawn = (ChessPiece) board.removePiece(next);
                Position pawnPosition;
                if(p.getColor() == Color.WHITE) {
                    pawnPosition = new Position(3, next.getColumn());
                }
                else{
                    pawnPosition = new Position(4, next.getColumn());
                }
                board.placePiece(pawn, pawnPosition);
                capturedPiece = board.removePiece(pawnPosition);
                capturedPieces.add(capturedPiece);
                piecesOnTheBoard.remove(capturedPiece);
            }
        }
    }

    private void validateSourcePosition(Position position) {
        if (!board.thereIsAPiece(position)) {
            throw new ChessException("There is no piece on this source position.");
        }
        if (currentPlayer != ((ChessPiece)board.piece(position)).getColor()) {
            throw new ChessException("The chosen piece is not yours.");
        }
        if (!board.piece(position).isThereAnyPossibleMove()) {
            throw new ChessException("There are no possible moves for the chosen piece.");
        }
    }

    private void validateNextPosition(Position source, Position next) {
        if(!board.piece(source).possibleMove(next)) {
            throw new ChessException("The chosen piece can't be moved to this position.");
        }
    }

    private void nextTurn() {
        turn++;
        currentPlayer = (currentPlayer == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private Color opponent(Color color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    private ChessPiece king(Color color) {
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).toList();
        for(Piece p : list) {
            if(p instanceof King) {
                return (ChessPiece) p;
            }
        }
        throw new IllegalStateException("There is no " + color + " king on the board.");
    }

    private boolean testCheck(Color color) {
        Position kingPosition = king(color).getChessPosition().toPosition();
        List<Piece> opponentPieces = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == opponent(color)).toList();
        for(Piece p : opponentPieces) {
            boolean[][] mat = p.possibleMoves();
            if(mat[kingPosition.getRow()][kingPosition.getColumn()]) {
                return true;
            }
        }
        return false;
    }

    private boolean testCheckMate(Color color) {
        if(!testCheck(color)) {
            return false;
        }
        List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece)x).getColor() == color).toList();
        for(Piece p : list) {
            boolean[][] mat = p.possibleMoves();
            for(int i = 0; i<board.getRows(); i++) {
                for(int j = 0; j<board.getColumns(); j++) {
                    if(mat[i][j]) {
                        Position source = ((ChessPiece)p).getChessPosition().toPosition();
                        Position next = new Position(i, j);
                        Piece capturedPiece = makeMove(source, next);
                        boolean testCheck = testCheck(color);
                        undoMove(source, next, capturedPiece);
                        if(!testCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void placeNewPiece(char column, int row, ChessPiece piece) {
        board.placePiece(piece, new ChessPosition(column, row).toPosition());
        piecesOnTheBoard.add(piece);
    }

    private void initalSetup() {
        placeNewPiece('a', 1, new Rook(board, Color.WHITE));
        placeNewPiece('b', 1, new Knight(board, Color.WHITE));
        placeNewPiece('c', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('d', 1, new Queen(board, Color.WHITE));
        placeNewPiece('e', 1, new King(board, Color.WHITE, this));
        placeNewPiece('f', 1, new Bishop(board, Color.WHITE));
        placeNewPiece('g', 1, new Knight(board, Color.WHITE));
        placeNewPiece('h', 1, new Rook(board, Color.WHITE));
        placeNewPiece('a', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('b', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('c', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('d', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('e', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('f', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('g', 2, new Pawn(board, Color.WHITE, this));
        placeNewPiece('h', 2, new Pawn(board, Color.WHITE, this));

        placeNewPiece('a', 8, new Rook(board, Color.BLACK));
        placeNewPiece('b', 8, new Knight(board, Color.BLACK));
        placeNewPiece('c', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('d', 8, new Queen(board, Color.BLACK));
        placeNewPiece('e', 8, new King(board, Color.BLACK, this));
        placeNewPiece('f', 8, new Bishop(board, Color.BLACK));
        placeNewPiece('g', 8, new Knight(board, Color.BLACK));
        placeNewPiece('h', 8, new Rook(board, Color.BLACK));
        placeNewPiece('a', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('b', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('c', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('d', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('e', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('f', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('g', 7, new Pawn(board, Color.BLACK, this));
        placeNewPiece('h', 7, new Pawn(board, Color.BLACK, this));
    }
}
