package modelAI;

import model.*;
import java.util.List;
import java.util.ArrayList;

public class ChessAI {
    private PieceColor aiColor;
    private int searchDepth;
    
    // Biến để lưu nước đi tốt nhất
    private Move bestMove;
    
    public ChessAI(PieceColor aiColor, int searchDepth) {
        this.aiColor = aiColor;
        this.searchDepth = searchDepth;
        this.bestMove = null;
    }
    
    public ChessAI(PieceColor aiColor) {
        this(aiColor, 2);
    }
    
    // Lấy nước đi tốt nhất
    public Move getBestMove(Board board, PieceColor currentPlayer) {
        bestMove = null;
        
        // Bắt đầu thuật toán minimax
        int value = minimax(true, board, searchDepth);
        
        System.out.println("AI chọn nước đi với giá trị: " + value);
        
        // Nếu không tìm được nước đi, chọn nước đi đầu tiên hợp lệ
        if (bestMove == null) {
            List<Move> moves = board.getLegalMoves(currentPlayer);
            if (!moves.isEmpty()) {
                bestMove = moves.get(0);
            }
        }
        
        return bestMove;
    }
    
    // Thuật toán Minimax đơn giản (giống pseudocode)
    public int minimax(boolean isMax, Board board, int depth) {
        // Điều kiện dừng
        if (depth == 0 || isTerminal(board)) {
            return evaluate(board);
        }
        
        // Xác định lượt đi hiện tại
        PieceColor currentPlayer;
        if (isMax) {
            currentPlayer = aiColor;  // AI đi
        } else {
            // Đối thủ đi
            currentPlayer = (aiColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
        }
        
        // Lấy tất cả nước đi hợp lệ
        List<Move> legalMoves = board.getLegalMoves(currentPlayer);
        
        // Nếu không có nước đi
        if (legalMoves.isEmpty()) {
            return evaluate(board);
        }
        
        if (isMax) {
            // MAX - AI
            int temp = -999999999;
            
            for (Move move : legalMoves) {
                // Tạo bàn cờ mới
                Board newBoard = board.cloneBoard();
                newBoard.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol, currentPlayer);
                
                // Gọi đệ quy
                int value = minimax(false, newBoard, depth - 1);
                
                // Cập nhật giá trị tốt nhất
                if (value > temp) {
                    temp = value;
                    
                    // Lưu lại nước đi tốt nhất (chỉ ở độ sâu cao nhất)
                    if (depth == searchDepth) {
                        bestMove = move;
                    }
                }
            }
            return temp;
            
        } else {
            // MIN - Đối thủ
            int temp = 999999999;
            
            for (Move move : legalMoves) {
                // Tạo bàn cờ mới
                Board newBoard = board.cloneBoard();
                newBoard.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol, currentPlayer);
                
                // Gọi đệ quy
                int value = minimax(true, newBoard, depth - 1);
                
                // Cập nhật giá trị tốt nhất
                if (value < temp) {
                    temp = value;
                }
            }
            return temp;
        }
    }
    
    // Đánh giá bàn cờ đơn giản
    private int evaluate(Board board) {
        int score = 0;
        
        // Đánh giá theo giá trị quân cờ
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece piece = board.getPiece(r, c);
                if (piece != null) {
                    int value = getPieceValue(piece);
                    
                    if (piece.getColor() == aiColor) {
                        score += value;  // Quân của AI
                    } else {
                        score -= value;  // Quân của đối thủ
                    }
                }
            }
        }
        
        return score;
    }
    
    // Lấy giá trị quân cờ
    private int getPieceValue(Piece piece) {
        char symbol = Character.toLowerCase(piece.getSymbol());
        
        switch (symbol) {
            case 'p': return 100;    // Tốt
            case 'n': return 320;    // Mã
            case 'b': return 330;    // Tượng
            case 'r': return 500;    // Xe
            case 'q': return 900;    // Hậu
            case 'k': return 20000;  // Vua
            default: return 0;
        }
    }
    
    // Kiểm tra trạng thái kết thúc
    private boolean isTerminal(Board board) {
        return board.isCheckmate(PieceColor.WHITE) || 
               board.isCheckmate(PieceColor.BLACK) || 
               board.isDraw();
    }
    
    // Getter và Setter
    public void setSearchDepth(int depth) {
        this.searchDepth = depth;
    }
    
    public int getSearchDepth() {
        return searchDepth;
    }
    
    public PieceColor getAiColor() {
        return aiColor;
    }
}