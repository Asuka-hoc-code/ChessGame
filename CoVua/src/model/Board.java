package model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Board {
    private Piece[][] board;
    private List<Move> moveHistory;
    
    // Tối ưu: Lưu vị trí vua để truy cập nhanh
    private Position whiteKingPosition;
    private Position blackKingPosition;
    
    // Tối ưu: Lưu trạng thái nhập thành trong bitmask
    private int castlingRights; // Bitmask: KQkq = 1111 (15), K=8, Q=4, k=2, q=1
    
    // Các hằng số cho castling rights
    private static final int WHITE_KINGSIDE = 8;  // 1000
    private static final int WHITE_QUEENSIDE = 4; // 0100
    private static final int BLACK_KINGSIDE = 2;  // 0010
    private static final int BLACK_QUEENSIDE = 1; // 0001
    
    // 50 nước không ăn quân
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
    
    // Theo dõi vị trí bàn cờ lặp lại
    private Map<String, Integer> positionCount = new HashMap<>();
    
    // Biến tạm để tối ưu hóa
    private List<Position> tempPositions = new ArrayList<>();

    public Board() {
        this(false);
    }

    // Constructor riêng cho clone
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
        // Xóa bàn cờ
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
        
        // Khởi tạo quyền nhập thành
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
    }

    public Piece getPiece(int row, int col) {
        if (!inBounds(row, col)) return null;
        return board[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if (!inBounds(row, col)) return;
        
        // Cập nhật vị trí vua nếu đặt vua
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

    // Tìm vua - sử dụng vị trí đã lưu
    public Position findKing(PieceColor color) {
        return (color == PieceColor.WHITE) ? whiteKingPosition : blackKingPosition;
    }

    // Kiểm tra chiếu - tối ưu hóa
    public boolean isInCheck(PieceColor kingColor) {
        Position kingPos = findKing(kingColor);
        if (kingPos == null) return false;

        PieceColor opponentColor = (kingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        
        // Kiểm tra tấn công từ các quân cờ
        return isSquareAttackedBy(kingPos.row, kingPos.col, opponentColor);
    }
    
    // Kiểm tra ô có bị tấn công không - tối ưu hóa
    private boolean isSquareAttackedBy(int row, int col, PieceColor attackerColor) {
        // 1. Kiểm tra mã
        if (isAttackedByKnight(row, col, attackerColor)) return true;
        
        // 2. Kiểm tra tốt
        if (isAttackedByPawn(row, col, attackerColor)) return true;
        
        // 3. Kiểm tra vua (ô liền kề)
        if (isAttackedByKing(row, col, attackerColor)) return true;
        
        // 4. Kiểm tra xe, hậu (ngang/dọc)
        if (isAttackedByRookOrQueen(row, col, attackerColor)) return true;
        
        // 5. Kiểm tra tượng, hậu (chéo)
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

    // Kiểm tra nước đi hợp lệ - tối ưu hóa
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        // 1. Kiểm tra ô bắt đầu có quân không
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null || piece.getColor() != playerColor) {
            return false;
        }

        // 2. Kiểm tra ô đích có quân cùng màu không
        Piece targetPiece = getPiece(toRow, toCol);
        if (targetPiece != null && targetPiece.getColor() == playerColor) {
            return false;
        }

        // 3. Kiểm tra nước đi có trong danh sách nước đi có thể của quân không
        List<Position> possibleMoves = piece.getPossibleMoves(this);
        boolean moveFound = false;
        for (Position pos : possibleMoves) {
            if (pos.row == toRow && pos.col == toCol) {
                moveFound = true;
                break;
            }
        }
        if (!moveFound) return false;

        // 4. Kiểm tra đặc biệt cho nhập thành
        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            return canCastle(playerColor, toCol > fromCol);
        }

        // 5. Tạm thời thực hiện nước đi để kiểm tra chiếu
        Piece captured = performTemporaryMove(fromRow, fromCol, toRow, toCol, piece, targetPiece);

        // 6. Kiểm tra sau khi đi, vua có bị chiếu không
        boolean inCheck = isInCheck(playerColor);

        // 7. Hoàn tác nước đi tạm thời
        undoMove(fromRow, fromCol, toRow, toCol, piece, captured);

        return !inCheck;
    }

    // Thực hiện nước đi tạm thời (cho việc kiểm tra) - tối ưu hóa
    private Piece performTemporaryMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, Piece targetPiece) {
        // Di chuyển quân
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        
        // Cập nhật vị trí vua nếu cần
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

    // Hoàn tác nước đi tạm thời
    private void undoMove(int fromRow, int fromCol, int toRow, int toCol, Piece piece, Piece captured) {
        // Đưa quân về vị trí cũ
        board[fromRow][fromCol] = piece;
        board[toRow][toCol] = captured;
        
        // Cập nhật vị trí vua nếu cần
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

    // Thực hiện nước đi thật
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol, playerColor)) {
            return false;
        }

        Piece piece = getPiece(fromRow, fromCol);
        Piece targetPiece = getPiece(toRow, toCol);

        // Cập nhật trạng thái đã di chuyển
        boolean hadMoved = piece.hasMoved();
        piece.setHasMoved(true);

        // Cập nhật trạng thái nhập thành
        updateCastlingRights(piece, fromRow, fromCol);

        // Tạo bản ghi nước đi
        Move move = new Move(fromRow, fromCol, toRow, toCol, piece, targetPiece);
        moveHistory.add(move);

        // Di chuyển quân
        board[toRow][toCol] = piece;
        board[fromRow][fromCol] = null;
        
        // Cập nhật vị trí vua nếu cần
        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingPosition = new Position(toRow, toCol);
            } else {
                blackKingPosition = new Position(toRow, toCol);
            }
        }
        
        piece.setPosition(toRow, toCol);

        // Xử lý nhập thành
        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            handleCastling(fromRow, fromCol, toRow, toCol);
        }

        // Cập nhật halfMoveClock
        if (piece instanceof Pawn || targetPiece != null) {
            halfMoveClock = 0; // Reset khi tốt di chuyển hoặc ăn quân
        } else {
            halfMoveClock++; // Tăng khi không có nước đi tốt hoặc ăn quân
        }

        // Cập nhật fullMoveNumber sau mỗi nước của đen
        if (playerColor == PieceColor.BLACK) {
            fullMoveNumber++;
        }

        // Ghi lại vị trí mới để kiểm tra lặp
        recordPosition();

        return true;
    }

    // Kiểm tra có thể nhập thành
    public boolean canCastle(PieceColor color, boolean kingside) {
        int row = (color == PieceColor.WHITE) ? 7 : 0;
        int kingCol = 4;
        int rookCol = kingside ? 7 : 0;
        int newKingCol = kingside ? 6 : 2;
        
        // Kiểm tra quyền nhập thành từ bitmask
        int requiredRight = (color == PieceColor.WHITE) ? 
            (kingside ? WHITE_KINGSIDE : WHITE_QUEENSIDE) :
            (kingside ? BLACK_KINGSIDE : BLACK_QUEENSIDE);
        
        if ((castlingRights & requiredRight) == 0) {
            return false;
        }

        // Kiểm tra vua và xe
        Piece king = getPiece(row, kingCol);
        Piece rook = getPiece(row, rookCol);
        
        if (!(king instanceof King) || !(rook instanceof Rook)) {
            return false;
        }

        // Kiểm tra vua không bị chiếu
        if (isInCheck(color)) {
            return false;
        }

        // Kiểm tra không có quân ở giữa
        int start = Math.min(kingCol, rookCol) + 1;
        int end = Math.max(kingCol, rookCol) - 1;
        for (int c = start; c <= end; c++) {
            if (!isEmpty(row, c)) {
                return false;
            }
        }

        // Kiểm tra các ô vua đi qua không bị tấn công
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
        // Nhập thành ngắn (kingside)
        if (toCol == 6) {
            Piece rook = getPiece(fromRow, 7);
            if (rook != null && rook instanceof Rook) {
                setPiece(fromRow, 5, rook);
                setPiece(fromRow, 7, null);
                rook.setHasMoved(true);
            }
        }
        // Nhập thành dài (queenside)
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
            if (row == 7) { // Hàng trắng
                if (col == 0) castlingRights &= ~WHITE_QUEENSIDE;
                if (col == 7) castlingRights &= ~WHITE_KINGSIDE;
            } else if (row == 0) { // Hàng đen
                if (col == 0) castlingRights &= ~BLACK_QUEENSIDE;
                if (col == 7) castlingRights &= ~BLACK_KINGSIDE;
            }
        }
        
        // Nếu quân bị ăn là xe, cũng cần cập nhật
        // (Điều này được xử lý trong undoLastMove)
    }

    // Hoàn tác nước đi
    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);

        // Hoàn tác di chuyển
        setPiece(lastMove.fromRow, lastMove.fromCol, lastMove.moved);
        setPiece(lastMove.toRow, lastMove.toCol, lastMove.captured);

        // Khôi phục trạng thái hasMoved
        if (lastMove.moved != null) {
            lastMove.moved.setHasMoved(lastMove.movedHadMoved);
        }
        if (lastMove.captured != null) {
            lastMove.captured.setHasMoved(lastMove.capturedHadMoved);
        }

        // Xử lý đặc biệt cho nhập thành
        if (lastMove.moved instanceof King && Math.abs(lastMove.fromCol - lastMove.toCol) == 2) {
            int row = lastMove.fromRow;
            
            // Nhập thành ngắn (kingside)
            if (lastMove.toCol == 6) {
                Piece rook = getPiece(row, 5);
                if (rook != null && rook instanceof Rook) {
                    setPiece(row, 7, rook);
                    setPiece(row, 5, null);
                    rook.setHasMoved(lastMove.movedHadMoved);
                    
                    // Khôi phục quyền nhập thành
                    if (row == 7) {
                        castlingRights |= WHITE_KINGSIDE;
                    } else if (row == 0) {
                        castlingRights |= BLACK_KINGSIDE;
                    }
                }
            }
            // Nhập thành dài (queenside)
            else if (lastMove.toCol == 2) {
                Piece rook = getPiece(row, 3);
                if (rook != null && rook instanceof Rook) {
                    setPiece(row, 0, rook);
                    setPiece(row, 3, null);
                    rook.setHasMoved(lastMove.movedHadMoved);
                    
                    // Khôi phục quyền nhập thành
                    if (row == 7) {
                        castlingRights |= WHITE_QUEENSIDE;
                    } else if (row == 0) {
                        castlingRights |= BLACK_QUEENSIDE;
                    }
                }
            }
            
            // Khôi phục quyền nhập thành của vua
            if (row == 7) {
                castlingRights |= (WHITE_KINGSIDE | WHITE_QUEENSIDE);
            } else if (row == 0) {
                castlingRights |= (BLACK_KINGSIDE | BLACK_QUEENSIDE);
            }
        } else {
            // Khôi phục quyền nhập thành nếu cần
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
            
            // Nếu quân bị ăn là xe, khôi phục quyền nhập thành
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

        // Giảm fullMoveNumber nếu vừa undo nước đi của đen
        if (!moveHistory.isEmpty()) {
            Move prevMove = moveHistory.get(moveHistory.size() - 1);
            if (prevMove.moved != null && prevMove.moved.getColor() == PieceColor.BLACK) {
                fullMoveNumber--;
            }
        } else {
            fullMoveNumber = 1;
        }

        // Xóa bản ghi vị trí hiện tại
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

    // Lấy tất cả nước đi hợp lệ - tối ưu hóa
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

    // Kiểm tra chiếu hết
    public boolean isCheckmate(PieceColor side) {
        return isInCheck(side) && getLegalMoves(side).isEmpty();
    }

    // Kiểm tra hết đường đi
    public boolean isStalemate(PieceColor side) {
        return !isInCheck(side) && getLegalMoves(side).isEmpty();
    }

    // Kiểm tra cờ hòa
    public boolean isDraw() {
        // 1. Stalemate
        if (isStalemate(PieceColor.WHITE) || isStalemate(PieceColor.BLACK)) {
            return true;
        }

        // 2. 50 nước không ăn quân
        if (halfMoveClock >= 100) {
            return true;
        }

        // 3. Không đủ lực lượng chiếu hết
        if (insufficientMaterial()) {
            return true;
        }

        // 4. Lặp lại vị trí 3 lần
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

        // Cả hai bên chỉ còn vua
        if (whiteCount == 1 && blackCount == 1) return true;

        // Một bên chỉ có vua, bên kia có vua + 1 tượng hoặc 1 mã
        if (whiteCount == 1 && blackCount == 2 && blackHasBishopOrKnight) return true;
        if (blackCount == 1 && whiteCount == 2 && whiteHasBishopOrKnight) return true;

        // Vua + Tượng vs Vua + Tượng (cùng màu ô)
        if (whiteCount == 2 && blackCount == 2) {
            if (whiteHasBishopOrKnight && blackHasBishopOrKnight) {
                // Tìm tượng
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

    // Kiểm tra lặp lại 3 lần
    private boolean isThreefoldRepetition() {
        String currentPos = getPositionString();
        return positionCount.containsKey(currentPos) && positionCount.get(currentPos) >= 3;
    }

    // Ghi nhận vị trí hiện tại
    private void recordPosition() {
        String pos = getPositionString();
        positionCount.put(pos, positionCount.getOrDefault(pos, 0) + 1);
    }

    // Tạo chuỗi đại diện cho vị trí bàn cờ
    private String getPositionString() {
        StringBuilder sb = new StringBuilder();

        // Thêm trạng thái các quân
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

        // Thêm lượt đi (dựa trên số nước đi)
        sb.append(moveHistory.size() % 2 == 0 ? 'w' : 'b');

        // Thêm quyền nhập thành
        if (castlingRights == 0) {
            sb.append('-');
        } else {
            if ((castlingRights & WHITE_KINGSIDE) != 0) sb.append('K');
            if ((castlingRights & WHITE_QUEENSIDE) != 0) sb.append('Q');
            if ((castlingRights & BLACK_KINGSIDE) != 0) sb.append('k');
            if ((castlingRights & BLACK_QUEENSIDE) != 0) sb.append('q');
        }

        // Thêm halfMoveClock
        sb.append(halfMoveClock);

        return sb.toString();
    }

    // Clone bàn cờ - tối ưu hóa
    public Board cloneBoard() {
        Board copy = new Board(true);

        // Clone tất cả các quân cờ
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    copy.board[r][c] = p.clonePiece();
                }
            }
        }

        // Clone lịch sử nước đi
        copy.moveHistory = new ArrayList<>();
        for (Move m : moveHistory) {
            copy.moveHistory.add(m.clone());
        }

        // Clone trạng thái
        copy.whiteKingPosition = new Position(whiteKingPosition.row, whiteKingPosition.col);
        copy.blackKingPosition = new Position(blackKingPosition.row, blackKingPosition.col);
        copy.castlingRights = this.castlingRights;
        copy.halfMoveClock = this.halfMoveClock;
        copy.fullMoveNumber = this.fullMoveNumber;
        copy.positionCount = new HashMap<>(this.positionCount);

        return copy;
    }

    // In bàn cờ
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

        // Hiển thị thông tin thêm
        System.out.println("Lượt: " + (moveHistory.size() % 2 == 0 ? "Trắng" : "Đen"));
        System.out.println("Nước: " + fullMoveNumber);
        System.out.println("Half-moves không ăn quân: " + halfMoveClock);
    }

    // Getters
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
    
    // Phương thức mới để lấy bàn cờ (cho AI)
    public Piece[][] getBoardArray() {
        Piece[][] copy = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, 8);
        }
        return copy;
    }
}