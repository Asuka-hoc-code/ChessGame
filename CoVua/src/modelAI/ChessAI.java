package modelAI;

import model.*;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class ChessAI {
    private PieceColor aiColor;
    private int searchDepth;
    private Move bestMove;
    private int nodesEvaluated = 0; 

    public ChessAI(PieceColor aiColor, int searchDepth) {
        this.aiColor = aiColor;
        this.searchDepth = searchDepth;
    }

    // Hàm gọi chính từ bên ngoài
    public Move getBestMove(Board board, PieceColor currentPlayer) {
        bestMove = null;
        nodesEvaluated = 0;
        long startTime = System.currentTimeMillis();

        System.out.println("AI đang tính toán (Alpha-Beta)...");

        // Gọi thuật toán Alpha-Beta
        minimaxAlphaBeta(true, board, searchDepth, Integer.MIN_VALUE, Integer.MAX_VALUE);

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("AI chốt nước đi sau: " + duration + "ms | Số node duyệt: " + nodesEvaluated);

        // tránh trường hợp ko có nước để đi thì ramdom từ các nước hợp lệ
        if (bestMove == null) {
            List<Move> moves = board.getLegalMoves(currentPlayer);
            if (!moves.isEmpty()) bestMove = moves.get(0);
        }
        
        return bestMove;
    }

    // Thuật toán Minimax kết hợp Alpha-Beta Pruning
    private int minimaxAlphaBeta(boolean isMax, Board board, int depth, int alpha, int beta) {
        nodesEvaluated++;

        if (depth == 0 || isTerminal(board)) {
            return evaluate(board, depth); 
        }

        PieceColor currentPlayer = isMax ? aiColor : (aiColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE);
        List<Move> legalMoves = board.getLegalMoves(currentPlayer);
        orderMoves(legalMoves);

        if (legalMoves.isEmpty()) return evaluate(board, depth);

        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
            	//  tạo node giả lập
                Board newBoard = board.cloneBoard();
                newBoard.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol, currentPlayer);
                
                int eval = minimaxAlphaBeta(false, newBoard, depth - 1, alpha, beta);
                
                if (eval > maxEval) {
                    maxEval = eval;
                    if (depth == searchDepth) bestMove = move;
                }
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                Board newBoard = board.cloneBoard();
                newBoard.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol, currentPlayer);
                
                int eval = minimaxAlphaBeta(true, newBoard, depth - 1, alpha, beta);
                
                if (eval < minEval) minEval = eval;
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluate(Board board, int depth) { 
        if (board.isCheckmate(getOpponentColor())) return 99999 + (depth * 100);  
        
        if (board.isCheckmate(aiColor)) return -99999 - (depth * 100); 

        // hòa hoặc lặp nước
        if (board.isDraw()) {
            int myMaterial = countMaterial(board, aiColor);
            int oppMaterial = countMaterial(board, getOpponentColor());

            if (myMaterial > oppMaterial + 200) { 
                return -1000; 
            }

            if (oppMaterial > myMaterial + 200) {
                return 500;
            }
            return 0;
        }

        int score = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null) {
                    int materialValue = getPieceValue(p);
                    int positionBonus = getPositionBonus(p, r, c);
                    
                    if (p instanceof King && isEndGame(board)) {
                        if (isCentral(r, c)) positionBonus += 40; 
                    }

                    int totalValue = materialValue + positionBonus;
                    if (p.getColor() == aiColor) score += totalValue;
                    else score -= totalValue;
                }
            }
        }
        return score;
    }

    private int countMaterial(Board board, PieceColor color) {
        int sum = 0;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = board.getPiece(r, c);
                if (p != null && p.getColor() == color) {
                    sum += getPieceValue(p);
                }
            }
        }
        return sum;
    }

    // Hàm nhận biết tàn cuộc (đơn giản: khi không còn Hậu hoặc ít quân)
    private boolean isEndGame(Board board) {
        return countMaterial(board, PieceColor.WHITE) + countMaterial(board, PieceColor.BLACK) < 1500;
    }

    private int getPositionBonus(Piece p, int r, int c) {
        int bonus = 0;
        
        if (p instanceof Knight) {
            if (isCentral(r, c)) bonus += 20;
            else if (isEdge(r, c)) bonus -= 10;
        }
        
        if (p instanceof Pawn) {
            int rank = (p.getColor() == PieceColor.WHITE) ? (7-r) : r; 
            bonus += rank * 5; 
            if (isCentral(r, c)) bonus += 15; 
        }

        // Quân Vua (King) nên trốn ở góc đầu game, ra giữa cuối game
        if (p instanceof King) {
            if (isCentral(r, c)) bonus -= 30;
        }

        return bonus;
    }

    private boolean isCentral(int r, int c) {
        return (r >= 3 && r <= 4 && c >= 3 && c <= 4);
    }
    
    private boolean isEdge(int r, int c) {
        return (r == 0 || r == 7 || c == 0 || c == 7);
    }

    private void orderMoves(List<Move> moves) {
        // Sắp xếp: Ưu tiên nước ăn quân đưa lên đầu
        Collections.sort(moves, (m1, m2) -> {
            int score1 = (m1.captured != null) ? getPieceValue(m1.captured) : 0;
            int score2 = (m2.captured != null) ? getPieceValue(m2.captured) : 0;
            return score2 - score1; // Giảm dần
        });
    }

    private int getPieceValue(Piece piece) {
        char symbol = Character.toLowerCase(piece.getSymbol());
        switch (symbol) {
            case 'p': return 100;
            case 'n': return 320;
            case 'b': return 330;
            case 'r': return 500;
            case 'q': return 900;
            case 'k': return 20000;
            default: return 0;
        }
    }
    
    private PieceColor getOpponentColor() {
        return aiColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
    }

    private boolean isTerminal(Board board) {
        return board.isCheckmate(PieceColor.WHITE) || 
               board.isCheckmate(PieceColor.BLACK) || 
               board.isDraw();
    }
    // minimax thuần
    private int minimaxStandard(boolean isMax, Board board, int depth) {
        nodesEvaluated++; 

        if (depth == 0 || isTerminal(board)) {
            return evaluate(board, depth);
        }

        PieceColor currentPlayer = isMax ? aiColor : (aiColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE);
        List<Move> legalMoves = board.getLegalMoves(currentPlayer);

        if (legalMoves.isEmpty()) return evaluate(board, depth);

        if (isMax) {
            int maxEval = Integer.MIN_VALUE;
            for (Move move : legalMoves) {
                Board newBoard = board.cloneBoard();
                newBoard.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol, currentPlayer);
                
                int eval = minimaxStandard(false, newBoard, depth - 1);
                maxEval = Math.max(maxEval, eval);
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (Move move : legalMoves) {
                Board newBoard = board.cloneBoard();
                newBoard.makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol, currentPlayer);
                
                int eval = minimaxStandard(true, newBoard, depth - 1);
                minEval = Math.min(minEval, eval);
            }
            return minEval;
        }
    }
    
    public void runBenchmark() {
        System.out.println(String.format("| %-5s | %-32s | %-32s | %-12s |", 
                "Depth", "Minimax (Time | Mem | Nodes)", "AlphaBeta (Time | Mem | Nodes)", "Tốc độ"));
        System.out.println("===================================================================================================================");

        Board board = new Board(); 

        for (int d = 1; d <= 6; d++) {
            

            System.gc();
            try { Thread.sleep(100); } catch (Exception e) {} 
            
            this.nodesEvaluated = 0;
            long startMemMM = getUsedMemory();
            long startTimeMM = System.currentTimeMillis();
            
            minimaxStandard(true, board, d); // Chạy thuật toán
            
            long timeMM = System.currentTimeMillis() - startTimeMM;
            long endMemMM = getUsedMemory();
            long memMM = Math.max(0, endMemMM - startMemMM) / 1024; // Đổi ra KB
            int nodesMM = this.nodesEvaluated;

            System.gc(); 
            try { Thread.sleep(100); } catch (Exception e) {}
            
            this.nodesEvaluated = 0;
            long startMemAB = getUsedMemory();
            long startTimeAB = System.currentTimeMillis();
            
            minimaxAlphaBeta(true, board, d, Integer.MIN_VALUE, Integer.MAX_VALUE); // Chạy thuật toán
            
            long timeAB = System.currentTimeMillis() - startTimeAB;
            long endMemAB = getUsedMemory();
            long memAB = Math.max(0, endMemAB - startMemAB) / 1024; // Đổi ra KB
            int nodesAB = this.nodesEvaluated;

            String speedUp = (timeAB == 0) ? "-" : String.format("%.1fx", (double)timeMM / timeAB);

            System.out.println(String.format("| %-5d | %6dms | %6dKB | %7d | %6dms | %6dKB | %7d | %-12s |", 
                    d, 
                    timeMM, memMM, nodesMM, 
                    timeAB, memAB, nodesAB, 
                    speedUp));
        }
     
    }
    
    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}