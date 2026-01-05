package model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Board {
    private Piece[][] board;
    private List<Move> moveHistory;

    private Position whiteKingPosition;
    private Position blackKingPosition;
    
    private int castlingRights; 
    
    private static final int WHITE_KINGSIDE = 8;  
    private static final int WHITE_QUEENSIDE = 4;
    private static final int BLACK_KINGSIDE = 2;  
    private static final int BLACK_QUEENSIDE = 1; 
    
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
    
    private Map<String, Integer> positionCount = new HashMap<>();
    
    private List<Position> tempPositions = new ArrayList<>();

    public Board() {
        this(false);
    }

    public Board(boolean skipSetup) {
        board = new Piece[8][8];
        moveHistory = new ArrayList<>();
        positionCount = new HashMap<>();
        if (!skipSetup) {
            setupDefaultBoard();
            recordPosition();
        }
    }

    private void setupDefaultBoard() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                board[r][c] = null;
            }
        }

        // Tốt
        for (int c = 0; c < 8; c++) {
            board[6][c] = new Pawn(PieceColor.WHITE, 6, c);
            board[1][c] = new Pawn(PieceColor.BLACK, 1, c);
        }

        // Xe
        board[7][0] = new Rook(PieceColor.WHITE, 7, 0);
        board[7][7] = new Rook(PieceColor.WHITE, 7, 7);
        board[0][0] = new Rook(PieceColor.BLACK, 0, 0);
        board[0][7] = new Rook(PieceColor.BLACK, 0, 7);

        // Mã
        board[7][1] = new Knight(PieceColor.WHITE, 7, 1);
        board[7][6] = new Knight(PieceColor.WHITE, 7, 6);
        board[0][1] = new Knight(PieceColor.BLACK, 0, 1);
        board[0][6] = new Knight(PieceColor.BLACK, 0, 6);

        // Tượng
        board[7][2] = new Bishop(PieceColor.WHITE, 7, 2);
        board[7][5] = new Bishop(PieceColor.WHITE, 7, 5);
        board[0][2] = new Bishop(PieceColor.BLACK, 0, 2);
        board[0][5] = new Bishop(PieceColor.BLACK, 0, 5);

        // Hậu
        board[7][3] = new Queen(PieceColor.WHITE, 7, 3);
        board[0][3] = new Queen(PieceColor.BLACK, 0, 3);

        // Vua
        board[7][4] = new King(PieceColor.WHITE, 7, 4);
        board[0][4] = new King(PieceColor.BLACK, 0, 4);
        
        // Lưu vị trí vua
        whiteKingPosition = new Position(7, 4);
        blackKingPosition = new Position(0, 4);
        
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
    }

    public Piece getPiece(int row, int col) {
        if (!inBounds(row, col)) return null;
        return board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (!inBounds(row, col)) return;
        
        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingPosition = new Position(row, col);
            } else {
                blackKingPosition = new Position(row, col);
            }
        }
        
        board[row][col] = piece;
        if (piece != null) {
            piece.setPosition(row, col);
        }
    }

    public boolean isEmpty(int row, int col) {
        return inBounds(row, col) && board[row][col] == null;
    }

    public boolean inBounds(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public Position findKing(PieceColor color) {
        return (color == PieceColor.WHITE) ? whiteKingPosition : blackKingPosition;
    }

    public boolean isInCheck(PieceColor kingColor) {
        Position kingPos = findKing(kingColor);
        if (kingPos == null) return false;

        PieceColor opponentColor = (kingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        
        return isSquareAttackedBy(kingPos.row, kingPos.col, opponentColor);
    }
    
    private boolean isSquareAttackedBy(int row, int col, PieceColor attackerColor) {
        if (isAttackedByKnight(row, col, attackerColor)) return true;
        
        if (isAttackedByPawn(row, col, attackerColor)) return true;

        if (isAttackedByKing(row, col, attackerColor)) return true;

        if (isAttackedByRookOrQueen(row, col, attackerColor)) return true;

        if (isAttackedByBishopOrQueen(row, col, attackerColor)) return true;
        
        return false;
    }
    
    private boolean isAttackedByKnight(int targetRow, int targetCol, PieceColor attackerColor) {
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };
        
        for (int[] move : knightMoves) {
            int r = targetRow + move[0];
            int c = targetCol + move[1];
            if (inBounds(r, c)) {
                Piece piece = board[r][c];
                if (piece instanceof Knight && piece.getColor() == attackerColor) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isAttackedByPawn(int targetRow, int targetCol, PieceColor attackerColor) {
        int direction = (attackerColor == PieceColor.WHITE) ? -1 : 1;
        int[] attackCols = {-1, 1};
        
        for (int dc : attackCols) {
            int r = targetRow - direction; // Tốt tấn công từ phía trước
            int c = targetCol + dc;
            if (inBounds(r, c)) {
                Piece piece = board[r][c];
                if (piece instanceof Pawn && piece.getColor() == attackerColor) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isAttackedByKing(int targetRow, int targetCol, PieceColor attackerColor) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = targetRow + dr;
                int c = targetCol + dc;
                if (inBounds(r, c)) {
                    Piece piece = board[r][c];
                    if (piece instanceof King && piece.getColor() == attackerColor) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private boolean isAttackedByRookOrQueen(int targetRow, int targetCol, PieceColor attackerColor) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int r = targetRow + dir[0];
            int c = targetCol + dir[1];
            
            while (inBounds(r, c)) {
                Piece piece = board[r][c];
                if (piece != null) {
                    if (piece.getColor() == attackerColor && 
                        (piece instanceof Rook || piece instanceof Queen)) {
                        return true;
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return false;
    }
    
    private boolean isAttackedByBishopOrQueen(int targetRow, int targetCol, PieceColor attackerColor) {
        int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        
        for (int[] dir : directions) {
            int r = targetRow + dir[0];
            int c = targetCol + dir[1];
            
            while (inBounds(r, c)) {
                Piece piece = board[r][c];
                if (piece != null) {
                    if (piece.getColor() == attackerColor && 
                        (piece instanceof Bishop || piece instanceof Queen)) {
                        return true;
                    }
                    break;
                }
                r += dir[0];
                c += dir[1];
            }
        }
        return false;
    }

    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null || piece.getColor() != playerColor) {
            return false;
        }

        Piece targetPiece = getPiece(toRow, toCol);
        if (targetPiece != null && targetPiece.getColor() == playerColor) {
            return false;
        }

        List<Position> possibleMoves = piece.getPossibleMoves(this);
        boolean moveFound = false;
        for (Position pos : possibleMoves) {
            if (pos.row == toRow && pos.col == toCol) {
                moveFound = true;
                break;
            }
        }
        if (!moveFound) return false;

        // Kiểm tra nhập thành
        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            return canCastle(playerColor, toCol > fromCol);
        }

        Piece captured = performTemporaryMove(fromRow, fromCol, toRow, toCol, piece, targetPiece);

        // check vua đi có bị chiếu khum
        boolean inCheck = isInCheck(playerColor);

        undoMove(fromRow, fromCol, toRow, toCol, piece, captured);

        return !inCheck;
    }

    private Piece performTemporaryMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, Piece targetPiece) {
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        
        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingPosition = new Position(toRow, toCol);
            } else {
                blackKingPosition = new Position(toRow, toCol);
            }
        }
        
        piece.setPosition(toRow, toCol);
        return targetPiece;
    }

    private void undoMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, Piece captured) {
        board[fromRow][fromCol] = piece;
        board[toRow][toCol] = captured;
        
        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingPosition = new Position(fromRow, fromCol);
            } else {
                blackKingPosition = new Position(fromRow, fromCol);
            }
        }
        
        piece.setPosition(fromRow, fromCol);
        if (captured != null) {
            captured.setPosition(toRow, toCol);
        }
    }

    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol, playerColor)) {
            return false;
        }

        Piece piece = getPiece(fromRow, fromCol);
        Piece targetPiece = getPiece(toRow, toCol);

        boolean hadMoved = piece.hasMoved();
        piece.setHasMoved(true);

        updateCastlingRights(piece, fromRow, fromCol);

        Move move = new Move(fromRow, fromCol, toRow, toCol, piece, targetPiece);
        moveHistory.add(move);

        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;

        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingPosition = new Position(toRow, toCol);
            } else {
                blackKingPosition = new Position(toRow, toCol);
            }
        }
        
        piece.setPosition(toRow, toCol);

        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            handleCastling(fromRow, fromCol, toRow, toCol);
        }

        if (piece instanceof Pawn || targetPiece != null) {
            halfMoveClock = 0; 
        } else {
            halfMoveClock++; 
        }

        if (playerColor == PieceColor.BLACK) {
            fullMoveNumber++;
        }

        recordPosition();

        return true;
    }

    public boolean canCastle(PieceColor color, boolean kingside) {
        int row = (color == PieceColor.WHITE) ? 7 : 0;
        int kingCol = 4;
        int rookCol = kingside ? 7 : 0;
        int newKingCol = kingside ? 6 : 2;

        int requiredRight = (color == PieceColor.WHITE) ? 
            (kingside ? WHITE_KINGSIDE : WHITE_QUEENSIDE) :
            (kingside ? BLACK_KINGSIDE : BLACK_QUEENSIDE);
        
        if ((castlingRights & requiredRight) == 0) {
            return false;
        }

        Piece king = getPiece(row, kingCol);
        Piece rook = getPiece(row, rookCol);
        
        if (!(king instanceof King) || !(rook instanceof Rook)) {
            return false;
        }

        if (isInCheck(color)) {
            return false;
        }

        int start = Math.min(kingCol, rookCol) + 1;
        int end = Math.max(kingCol, rookCol) - 1;
        for (int c = start; c <= end; c++) {
            if (!isEmpty(row, c)) {
                return false;
            }
        }

        int step = kingside ? 1 : -1;
        for (int c = kingCol + step; c != newKingCol + step; c += step) {
            if (c == kingCol) continue;
            if (isSquareAttackedBy(row, c, color == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE)) {
                return false;
            }
        }

        return true;
    }

    private void handleCastling(int fromRow, int fromCol, int toRow, int toCol) {
        // Nhập thành ngắn 
        if (toCol == 6) {
            Piece rook = getPiece(fromRow, 7);
            if (rook != null && rook instanceof Rook) {
                setPiece(fromRow, 5, rook);
                setPiece(fromRow, 7, null);
                rook.setHasMoved(true);
            }
        }
        // Nhập thành dài
        else if (toCol == 2) {
            Piece rook = getPiece(fromRow, 0);
            if (rook != null && rook instanceof Rook) {
                setPiece(fromRow, 3, rook);
                setPiece(fromRow, 0, null);
                rook.setHasMoved(true);
            }
        }
    }

    private void updateCastlingRights(Piece piece, int row, int col) {
        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                castlingRights &= ~(WHITE_KINGSIDE | WHITE_QUEENSIDE);
            } else {
                castlingRights &= ~(BLACK_KINGSIDE | BLACK_QUEENSIDE);
            }
        } else if (piece instanceof Rook) {
            if (row == 7) { 
                if (col == 0) castlingRights &= ~WHITE_QUEENSIDE;
                if (col == 7) castlingRights &= ~WHITE_KINGSIDE;
            } else if (row == 0) { 
                if (col == 0) castlingRights &= ~BLACK_QUEENSIDE;
                if (col == 7) castlingRights &= ~BLACK_KINGSIDE;
            }
        }
    }

    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        setPiece(lastMove.fromRow, lastMove.fromCol, lastMove.moved);
        setPiece(lastMove.toRow, lastMove.toCol, lastMove.captured);

        if (lastMove.moved != null) {
            lastMove.moved.setHasMoved(lastMove.movedHadMoved);
        }
        if (lastMove.captured != null) {
            lastMove.captured.setHasMoved(lastMove.capturedHadMoved);
        }

        if (lastMove.moved instanceof King && Math.abs(lastMove.fromCol - lastMove.toCol) == 2) {
            int row = lastMove.fromRow;
            
            // Nhập thành ngắn 
            if (lastMove.toCol == 6) {
                Piece rook = getPiece(row, 5);
                if (rook != null && rook instanceof Rook) {
                    setPiece(row, 7, rook);
                    setPiece(row, 5, null);
                    rook.setHasMoved(lastMove.movedHadMoved);

                    if (row == 7) {
                        castlingRights |= WHITE_KINGSIDE;
                    } else if (row == 0) {
                        castlingRights |= BLACK_KINGSIDE;
                    }
                }
            }
            // Nhập thành dài 
            else if (lastMove.toCol == 2) {
                Piece rook = getPiece(row, 3);
                if (rook != null && rook instanceof Rook) {
                    setPiece(row, 0, rook);
                    setPiece(row, 3, null);
                    rook.setHasMoved(lastMove.movedHadMoved);

                    if (row == 7) {
                        castlingRights |= WHITE_QUEENSIDE;
                    } else if (row == 0) {
                        castlingRights |= BLACK_QUEENSIDE;
                    }
                }
            }

            if (row == 7) {
                castlingRights |= (WHITE_KINGSIDE | WHITE_QUEENSIDE);
            } else if (row == 0) {
                castlingRights |= (BLACK_KINGSIDE | BLACK_QUEENSIDE);
            }
        } else {
            if (lastMove.moved instanceof Rook) {
                if (lastMove.moved.getColor() == PieceColor.WHITE) {
                    if (lastMove.fromRow == 7 && lastMove.fromCol == 0) castlingRights |= WHITE_QUEENSIDE;
                    if (lastMove.fromRow == 7 && lastMove.fromCol == 7) castlingRights |= WHITE_KINGSIDE;
                } else {
                    if (lastMove.fromRow == 0 && lastMove.fromCol == 0) castlingRights |= BLACK_QUEENSIDE;
                    if (lastMove.fromRow == 0 && lastMove.fromCol == 7) castlingRights |= BLACK_KINGSIDE;
                }
            } else if (lastMove.moved instanceof King) {
                if (lastMove.moved.getColor() == PieceColor.WHITE) {
                    castlingRights |= (WHITE_KINGSIDE | WHITE_QUEENSIDE);
                } else {
                    castlingRights |= (BLACK_KINGSIDE | BLACK_QUEENSIDE);
                }
            }
            
            if (lastMove.captured instanceof Rook) {
                if (lastMove.captured.getColor() == PieceColor.WHITE) {
                    if (lastMove.toRow == 7 && lastMove.toCol == 0) castlingRights |= WHITE_QUEENSIDE;
                    if (lastMove.toRow == 7 && lastMove.toCol == 7) castlingRights |= WHITE_KINGSIDE;
                } else {
                    if (lastMove.toRow == 0 && lastMove.toCol == 0) castlingRights |= BLACK_QUEENSIDE;
                    if (lastMove.toRow == 0 && lastMove.toCol == 7) castlingRights |= BLACK_KINGSIDE;
                }
            }
        }

        if (!moveHistory.isEmpty()) {
            Move prevMove = moveHistory.get(moveHistory.size() - 1);
            if (prevMove.moved != null && prevMove.moved.getColor() == PieceColor.BLACK) {
                fullMoveNumber--;
            }
        } else {
            fullMoveNumber = 1;
        }

        String currentPos = getPositionString();
        if (positionCount.containsKey(currentPos)) {
            int count = positionCount.get(currentPos);
            if (count > 1) {
                positionCount.put(currentPos, count - 1);
            } else {
                positionCount.remove(currentPos);
            }
        }

        return true;
    }

    // Lấy tất cả nước đi hợp lệ 
    public List<Move> getLegalMoves(PieceColor side) {
        tempPositions.clear();
        List<Move> legalMoves = new ArrayList<>();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == side) {
                    tempPositions = p.getPossibleMoves(this);
                    for (Position pos : tempPositions) {
                        if (isValidMove(r, c, pos.row, pos.col, side)) {
                            Piece captured = getPiece(pos.row, pos.col);
                            legalMoves.add(new Move(r, c, pos.row, pos.col, p, captured));
                        }
                    }
                }
            }
        }

        return legalMoves;
    }

    public boolean isCheckmate(PieceColor side) {
        return isInCheck(side) && getLegalMoves(side).isEmpty();
    }

    public boolean isStalemate(PieceColor side) {
        return !isInCheck(side) && getLegalMoves(side).isEmpty();
    }

    public boolean isDraw() {
        if (isStalemate(PieceColor.WHITE) || isStalemate(PieceColor.BLACK)) {
            return true;
        }

        if (halfMoveClock >= 100) {
            return true;
        }

        //Không đủ quân chiếu
        if (insufficientMaterial()) {
            return true;
        }

        //Lặp lại vị trí 3 lần
        if (isThreefoldRepetition()) {
            return true;
        }

        return false;
    }

    private boolean insufficientMaterial() {
        int whiteCount = 0, blackCount = 0;
        boolean whiteHasNonKing = false, blackHasNonKing = false;
        boolean whiteHasBishopOrKnight = false, blackHasBishopOrKnight = false;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    if (p.getColor() == PieceColor.WHITE) {
                        whiteCount++;
                        if (!(p instanceof King)) {
                            whiteHasNonKing = true;
                            if (p instanceof Bishop || p instanceof Knight) {
                                whiteHasBishopOrKnight = true;
                            }
                        }
                    } else {
                        blackCount++;
                        if (!(p instanceof King)) {
                            blackHasNonKing = true;
                            if (p instanceof Bishop || p instanceof Knight) {
                                blackHasBishopOrKnight = true;
                            }
                        }
                    }
                }
            }
        }

        if (whiteCount == 1 && blackCount == 1) return true;

        if (whiteCount == 1 && blackCount == 2 && blackHasBishopOrKnight) return true;
        if (blackCount == 1 && whiteCount == 2 && whiteHasBishopOrKnight) return true;

        if (whiteCount == 2 && blackCount == 2) {
            if (whiteHasBishopOrKnight && blackHasBishopOrKnight) {
                Bishop whiteBishop = null, blackBishop = null;
                for (int r = 0; r < 8; r++) {
                    for (int c = 0; c < 8; c++) {
                        Piece p = board[r][c];
                        if (p instanceof Bishop) {
                            if (p.getColor() == PieceColor.WHITE) whiteBishop = (Bishop) p;
                            else blackBishop = (Bishop) p;
                        }
                    }
                }
                
                if (whiteBishop != null && blackBishop != null) {
                    boolean whiteOnLight = (whiteBishop.getRow() + whiteBishop.getCol()) % 2 == 0;
                    boolean blackOnLight = (blackBishop.getRow() + blackBishop.getCol()) % 2 == 0;
                    if (whiteOnLight == blackOnLight) return true;
                }
            }
        }

        return false;
    }

    private boolean isThreefoldRepetition() {
        String currentPos = getPositionString();
        return positionCount.containsKey(currentPos) && positionCount.get(currentPos) >= 3;
    }

    private void recordPosition() {
        String pos = getPositionString();
        positionCount.put(pos, positionCount.getOrDefault(pos, 0) + 1);
    }

    private String getPositionString() {
        StringBuilder sb = new StringBuilder();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    sb.append(p.getSymbol());
                } else {
                    sb.append('.');
                }
            }
        }

        sb.append(moveHistory.size() % 2 == 0 ? 'w' : 'b');

        if (castlingRights == 0) {
            sb.append('-');
        } else {
            if ((castlingRights & WHITE_KINGSIDE) != 0) sb.append('K');
            if ((castlingRights & WHITE_QUEENSIDE) != 0) sb.append('Q');
            if ((castlingRights & BLACK_KINGSIDE) != 0) sb.append('k');
            if ((castlingRights & BLACK_QUEENSIDE) != 0) sb.append('q');
        }

        sb.append(halfMoveClock);

        return sb.toString();
    }

    public Board cloneBoard() {
        Board copy = new Board(true);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    copy.board[r][c] = p.clonePiece();
                }
            }
        }
        // tạo nháp lịch sử nước đii
        copy.moveHistory = new ArrayList<>();
        for (Move m : moveHistory) {
            copy.moveHistory.add(m.clone());
        }


        copy.whiteKingPosition = new Position(whiteKingPosition.row, whiteKingPosition.col);
        copy.blackKingPosition = new Position(blackKingPosition.row, blackKingPosition.col);
        copy.castlingRights = this.castlingRights;
        copy.halfMoveClock = this.halfMoveClock;
        copy.fullMoveNumber = this.fullMoveNumber;
        copy.positionCount = new HashMap<>(this.positionCount);

        return copy;
    }

    public void printBoard() {
        System.out.println("  a b c d e f g h");
        System.out.println("  ----------------");

        for (int r = 0; r < 8; r++) {
            System.out.print((8 - r) + "|");
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                System.out.print((p == null ? ". " : p.getSymbol() + " "));
            }
            System.out.println("|" + (8 - r));
        }

        System.out.println("  ----------------");
        System.out.println("  a b c d e f g h");
        System.out.println();

        System.out.println("Lượt: " + (moveHistory.size() % 2 == 0 ? "Trắng" : "Đen"));
        System.out.println("Nước: " + fullMoveNumber);
        System.out.println("Half-moves không ăn quân: " + halfMoveClock);
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public int getFullMoveNumber() {
        return fullMoveNumber;
    }

    public boolean canPromotePawn(int row, int col) {
        Piece piece = getPiece(row, col);
        if (!(piece instanceof Pawn)) return false;

        int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
        return row == promotionRow;
    }

    // Phong cấp tốt
    public void promotePawn(int row, int col, Piece newPiece) {
        if (inBounds(row, col) && board[row][col] instanceof Pawn) {
            board[row][col] = newPiece;
            newPiece.setPosition(row, col);
            newPiece.setHasMoved(true);
        }
    }
    
    public Piece[][] getBoardArray() {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, 8);
        }
        return copy;
    }
}