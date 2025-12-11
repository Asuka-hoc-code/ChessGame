package model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class Board {
    private Piece[][] board;
    private List<Move> moveHistory;
    
    // Biến theo dõi trạng thái nhập thành
    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteRookKingSideMoved = false;
    private boolean whiteRookQueenSideMoved = false;
    private boolean blackRookKingSideMoved = false;
    private boolean blackRookQueenSideMoved = false;
    
    // Biến theo dõi 50 nước không ăn quân
    private int halfMoveClock = 0;
    private int fullMoveNumber = 1;
    
    // Theo dõi vị trí bàn cờ lặp lại
    private Map<String, Integer> positionCount = new HashMap<>();
    
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
    
    // Khởi tạo bàn cờ mặc định
    private void setupDefaultBoard() {
        // Xóa tất cả quân
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
    }
    
    // Getter/Setter
    public Piece getPiece(int row, int col) {
        if (!inBounds(row, col)) return null;
        return board[row][col];
    }
    
    public void setPiece(int row, int col, Piece piece) {
        if (!inBounds(row, col)) return;
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
    
    // Tìm vua
    public Position findKing(PieceColor color) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == color && p instanceof King) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }
    
    // Kiểm tra chiếu
    public boolean isInCheck(PieceColor kingColor) {
        Position kingPos = findKing(kingColor);
        if (kingPos == null) return false;
        
        PieceColor opponentColor = (kingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        
        // Kiểm tra xem có quân đối phương nào tấn công được vua không
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == opponentColor) {
                    List<Position> moves = p.getPossibleMoves(this);
                    if (moves != null) {
                        for (Position move : moves) {
                            if (move.row == kingPos.row && move.col == kingPos.col) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
    
    // Kiểm tra nước đi hợp lệ
    public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        // 1. Kiểm tra ô bắt đầu có quân không
        Piece piece = getPiece(fromRow, fromCol);
        if (piece == null) {
            return false;
        }
        
        // 2. Kiểm tra quân này có màu của người chơi hiện tại không
        if (piece.getColor() != playerColor) {
            return false;
        }
        
        // 3. Kiểm tra ô đích có quân cùng màu không
        Piece targetPiece = getPiece(toRow, toCol);
        if (targetPiece != null && targetPiece.getColor() == playerColor) {
            return false;
        }
        
        // 4. Kiểm tra nước đi có trong danh sách nước đi có thể của quân không
        List<Position> possibleMoves = piece.getPossibleMoves(this);
        boolean moveFound = false;
        for (Position pos : possibleMoves) {
            if (pos.row == toRow && pos.col == toCol) {
                moveFound = true;
                break;
            }
        }
        
        if (!moveFound) {
            return false;
        }
        
        // 5. Tạo bản sao bàn cờ để thử nghiệm
        Board tempBoard = this.cloneBoard();
        Piece tempPiece = tempBoard.getPiece(fromRow, fromCol);
        Piece capturedPiece = tempBoard.getPiece(toRow, toCol);
        
        // Thực hiện nước đi trên bản sao
        Move tempMove = new Move(fromRow, fromCol, toRow, toCol, tempPiece, capturedPiece);
        tempBoard.applyMove(tempMove);
        
        // QUAN TRỌNG: Cập nhật trạng thái đã di chuyển của quân
        if (tempPiece != null) {
            tempPiece.setHasMoved(true);
        }
        
        // 6. Kiểm tra sau khi đi, vua có bị chiếu không
        if (tempBoard.isInCheck(playerColor)) {
            return false;
        }
        
        return true;
    }
    
    // Thực hiện nước đi
    public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
        if (!isValidMove(fromRow, fromCol, toRow, toCol, playerColor)) {
            return false;
        }
        
        Piece piece = getPiece(fromRow, fromCol);
        Piece capturedPiece = getPiece(toRow, toCol);
        
        // Cập nhật trạng thái đã di chuyển của quân
        if (piece != null) {
            piece.setHasMoved(true);
        }
        
        // Cập nhật trạng thái di chuyển của vua và xe
        updateCastlingStatus(piece, fromRow, fromCol);
        
        // Lưu lại nước đi
        Move move = new Move(fromRow, fromCol, toRow, toCol, piece, capturedPiece);
        moveHistory.add(move);
        
        // Thực hiện nước đi
        applyMove(move);
        
        // Xử lý nhập thành
        if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
            handleCastling(fromRow, fromCol, toRow, toCol);
        }
        
        // Xử lý tốt
        if (piece instanceof Pawn) {
            // Cập nhật halfMoveClock (reset vì tốt di chuyển)
            halfMoveClock = 0;
        } else {
            // Cập nhật halfMoveClock
            if (capturedPiece != null) {
                halfMoveClock = 0; // Reset khi có ăn quân
            } else {
                halfMoveClock++; // Tăng khi không có ăn quân
            }
        }
        
        // Cập nhật fullMoveNumber sau mỗi nước của đen
        if (playerColor == PieceColor.BLACK) {
            fullMoveNumber++;
        }
        
        // Ghi lại vị trí mới để kiểm tra lặp
        recordPosition();
        
        return true;
    }
    
    // Phong cấp tốt
    public void promotePawn(int row, int col, Piece newPiece) {
        if (inBounds(row, col) && board[row][col] instanceof Pawn) {
            board[row][col] = newPiece;
            newPiece.setPosition(row, col);
            newPiece.setHasMoved(true);
        }
    }
    
    // Kiểm tra có thể nhập thành
    public boolean canCastle(PieceColor color, boolean kingside) {
        int row = (color == PieceColor.WHITE) ? 7 : 0;
        int kingCol = 4;
        int rookCol = kingside ? 7 : 0;
        int newKingCol = kingside ? 6 : 2;
        int rookNewCol = kingside ? 5 : 3;
        
        // 1. Kiểm tra vua và xe chưa di chuyển
        if (color == PieceColor.WHITE) {
            if (whiteKingMoved) return false;
            if (kingside && whiteRookKingSideMoved) return false;
            if (!kingside && whiteRookQueenSideMoved) return false;
        } else {
            if (blackKingMoved) return false;
            if (kingside && blackRookKingSideMoved) return false;
            if (!kingside && blackRookQueenSideMoved) return false;
        }
        
        // 2. Kiểm tra không có quân nào giữa vua và xe
        int start = Math.min(kingCol, rookCol) + 1;
        int end = Math.max(kingCol, rookCol) - 1;
        for (int c = start; c <= end; c++) {
            if (!isEmpty(row, c)) return false;
        }
        
        // 3. Vua không bị chiếu
        if (isInCheck(color)) return false;
        
        // 4. Vua không đi qua ô bị chiếu
        int step = kingside ? 1 : -1;
        for (int c = kingCol + step; c != newKingCol; c += step) {
            Board tempBoard = this.cloneBoard();
            tempBoard.setPiece(row, kingCol, null);
            tempBoard.setPiece(row, c, new King(color, row, c));
            if (tempBoard.isInCheck(color)) {
                return false;
            }
        }
        
        return true;
    }
    
    private void handleCastling(int fromRow, int fromCol, int toRow, int toCol) {
        // Nhập thành ngắn (kingside)
        if (toCol == 6) {
            // Di chuyển xe từ (fromRow, 7) sang (fromRow, 5)
            Piece rook = getPiece(fromRow, 7);
            if (rook != null && rook instanceof Rook) {
                board[fromRow][5] = rook;
                board[fromRow][7] = null;
                rook.setPosition(fromRow, 5);
                rook.setHasMoved(true);
            }
        }
        // Nhập thành dài (queenside)
        else if (toCol == 2) {
            // Di chuyển xe từ (fromRow, 0) sang (fromRow, 3)
            Piece rook = getPiece(fromRow, 0);
            if (rook != null && rook instanceof Rook) {
                board[fromRow][3] = rook;
                board[fromRow][0] = null;
                rook.setPosition(fromRow, 3);
                rook.setHasMoved(true);
            }
        }
    }
    
    private void updateCastlingStatus(Piece piece, int row, int col) {
        if (piece instanceof King) {
            if (piece.getColor() == PieceColor.WHITE) {
                whiteKingMoved = true;
            } else {
                blackKingMoved = true;
            }
        } else if (piece instanceof Rook) {
            if (row == 7) { // Hàng trắng
                if (col == 0) whiteRookQueenSideMoved = true;
                if (col == 7) whiteRookKingSideMoved = true;
            } else if (row == 0) { // Hàng đen
                if (col == 0) blackRookQueenSideMoved = true;
                if (col == 7) blackRookKingSideMoved = true;
            }
        }
    }
    
    // Hoàn tác nước đi
    public boolean undoLastMove() {
        if (moveHistory.isEmpty()) {
            return false;
        }
        
        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        undoMove(lastMove);
        
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
    
    // Lấy tất cả nước đi hợp lệ
    public List<Move> getLegalMoves(PieceColor side) {
        List<Move> legalMoves = new ArrayList<>();
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null && p.getColor() == side) {
                    List<Position> positions = p.getPossibleMoves(this);
                    for (Position pos : positions) {
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
        
        // 2. 50 nước không ăn quân (halfMoveClock đếm mỗi nước không ăn, cần 100 half-moves = 50 full moves)
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
    
    // Kiểm tra không đủ lực lượng chiếu hết - ĐÃ CẢI THIỆN
    private boolean insufficientMaterial() {
        List<Piece> whitePieces = new ArrayList<>();
        List<Piece> blackPieces = new ArrayList<>();
        
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    if (p.getColor() == PieceColor.WHITE) {
                        whitePieces.add(p);
                    } else {
                        blackPieces.add(p);
                    }
                }
            }
        }
        
        // Cả hai bên chỉ còn vua
        if (whitePieces.size() == 1 && blackPieces.size() == 1) {
            return true;
        }
        
        // Một bên chỉ có vua, bên kia có vua + 1 tượng hoặc 1 mã
        if (whitePieces.size() == 1 && blackPieces.size() == 2) {
            for (Piece p : blackPieces) {
                if (p instanceof Bishop || p instanceof Knight) {
                    return true;
                }
            }
        }
        if (blackPieces.size() == 1 && whitePieces.size() == 2) {
            for (Piece p : whitePieces) {
                if (p instanceof Bishop || p instanceof Knight) {
                    return true;
                }
            }
        }
        
        // Vua + Tượng vs Vua + Tượng (cùng màu ô) - CẢI THIỆN
        if (whitePieces.size() == 2 && blackPieces.size() == 2) {
            boolean whiteHasOnlyKingAndBishop = false;
            boolean blackHasOnlyKingAndBishop = false;
            Piece whiteBishop = null;
            Piece blackBishop = null;
            
            if (hasOnlyKingAndBishop(whitePieces)) {
                whiteHasOnlyKingAndBishop = true;
                whiteBishop = getBishopFromList(whitePieces);
            }
            if (hasOnlyKingAndBishop(blackPieces)) {
                blackHasOnlyKingAndBishop = true;
                blackBishop = getBishopFromList(blackPieces);
            }
            
            if (whiteHasOnlyKingAndBishop && blackHasOnlyKingAndBishop) {
                // Kiểm tra tượng có cùng màu ô không
                boolean whiteBishopOnLightSquare = isOnLightSquare(whiteBishop);
                boolean blackBishopOnLightSquare = isOnLightSquare(blackBishop);
                
                if (whiteBishopOnLightSquare == blackBishopOnLightSquare) {
                    return true; // Cùng màu ô -> hòa
                }
            }
        }
        
        return false;
    }
    
    private boolean hasOnlyKingAndBishop(List<Piece> pieces) {
        if (pieces.size() != 2) return false;
        boolean hasKing = false;
        boolean hasBishop = false;
        
        for (Piece p : pieces) {
            if (p instanceof King) hasKing = true;
            if (p instanceof Bishop) hasBishop = true;
        }
        
        return hasKing && hasBishop;
    }
    
    private Piece getBishopFromList(List<Piece> pieces) {
        for (Piece p : pieces) {
            if (p instanceof Bishop) return p;
        }
        return null;
    }
    
    private boolean isOnLightSquare(Piece piece) {
        if (piece == null) return false;
        // Ô sáng có (row + col) là số chẵn, ô tối có (row + col) là số lẻ
        return (piece.getRow() + piece.getCol()) % 2 == 0;
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
    
    // Tạo chuỗi đại diện cho vị trí bàn cờ - ĐÃ SỬA
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
        
        // Thêm lượt đi
        sb.append(moveHistory.size() % 2 == 0 ? 'w' : 'b');
        
        // Thêm quyền nhập thành - ĐÃ THÊM TRẠNG THÁI ĐEN
        sb.append(whiteKingMoved ? '-' : 'K');
        sb.append(whiteRookKingSideMoved ? '-' : 'k');
        sb.append(whiteRookQueenSideMoved ? '-' : 'Q');
        sb.append(blackKingMoved ? '-' : 'q');
        sb.append(blackRookKingSideMoved ? '-' : 'K');
        sb.append(blackRookQueenSideMoved ? '-' : 'Q');
        
        // Thêm dấu hiệu (không có enPassant)
        sb.append('-');
        
        // Thêm halfMoveClock
        sb.append('-');
        sb.append(halfMoveClock);
        
        return sb.toString();
    }
    
    // Apply/Undo move
    public void applyMove(Move m) {
        setPiece(m.toRow, m.toCol, m.moved);
        setPiece(m.fromRow, m.fromCol, null);
    }
    
    public void undoMove(Move m) {
        setPiece(m.fromRow, m.fromCol, m.moved);
        setPiece(m.toRow, m.toCol, m.captured);
    }
    
    // Clone bàn cờ - ĐÃ SỬA
    public Board cloneBoard() {
        Board copy = new Board(true); // Không gọi setupDefaultBoard
        
        // Clone tất cả các quân cờ
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board[r][c];
                if (p != null) {
                    copy.board[r][c] = p.clonePiece();
                } else {
                    copy.board[r][c] = null;
                }
            }
        }
        
        copy.moveHistory = new ArrayList<>();
        for (Move m : moveHistory) {
            copy.moveHistory.add(m.clone());
        }
        
        copy.whiteKingMoved = this.whiteKingMoved;
        copy.blackKingMoved = this.blackKingMoved;
        copy.whiteRookKingSideMoved = this.whiteRookKingSideMoved;
        copy.whiteRookQueenSideMoved = this.whiteRookQueenSideMoved;
        copy.blackRookKingSideMoved = this.blackRookKingSideMoved;
        copy.blackRookQueenSideMoved = this.blackRookQueenSideMoved;
        
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
    
    public boolean isWhiteKingMoved() {
        return whiteKingMoved;
    }
    
    public boolean isBlackKingMoved() {
        return blackKingMoved;
    }
    
    // Kiểm tra có thể phong cấp
    public boolean canPromotePawn(int row, int col) {
        Piece piece = getPiece(row, col);
        if (!(piece instanceof Pawn)) return false;
        
        int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
        return row == promotionRow;
    }
}