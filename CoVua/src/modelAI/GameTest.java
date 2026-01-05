package modelAI;

import java.util.List;
import java.util.Scanner;

import model.Board;
import model.Move;
import model.PieceColor;

public class GameTest {
    private Board board;
    private PieceColor currentPlayer;
    private boolean gameOver;
    private Scanner scanner;
    private String winner;
    
    public GameTest() {
        board = new Board(false);
        currentPlayer = PieceColor.WHITE; // Trắng đi trước
        gameOver = false;
        winner = null;
        scanner = new Scanner(System.in);
    }
    
    public void start() {
        System.out.println("===== CHESS GAME =====");
        System.out.println("Nhập nước đi theo định dạng: e2 e4 (từ ô e2 đến e4)");
        System.out.println("Các lệnh đặc biệt:");
        System.out.println("  - 'undo': hoàn tác nước đi cuối");
        System.out.println("  - 'moves': xem các nước đi hợp lệ");
        System.out.println("  - 'quit': thoát game");
        System.out.println("  - 'resign': đầu hàng");
        System.out.println();
        
        board.printBoard();
        
        while (!gameOver) {
            printGameState();
            
            System.out.print(currentPlayer + " đi: ");
            String input = scanner.nextLine().trim();
            
            if (input.equalsIgnoreCase("quit")) {
                System.out.println("Kết thúc trò chơi!");
                break;
            }
            else if (input.equalsIgnoreCase("resign")) {
                resign();
                continue;
            }
            else if (input.equalsIgnoreCase("undo")) {
                undoMove();
                continue;
            }
            else if (input.equalsIgnoreCase("moves")) {
                showLegalMoves();
                continue;
            }
            
            if (processMove(input)) {
                checkGameEnd();
                
                if (!gameOver) {
                    currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                                    PieceColor.BLACK : PieceColor.WHITE;
                }
            } else {
                System.out.println("Nước đi không hợp lệ! Vui lòng thử lại.");
                System.out.println("Gợi ý: Định dạng đúng là 'e2 e4' hoặc 'e7e5'");
            }
        }
        
        printGameResult();
        scanner.close();
    }
    
    private boolean processMove(String input) {
        String cleanInput = input.replaceAll("\\s+", "");
        
        if (cleanInput.length() != 4) {
            return false;
        }
        
        try {
            int fromCol = convertColumn(cleanInput.charAt(0));
            int fromRow = 8 - Character.getNumericValue(cleanInput.charAt(1));
            
            int toCol = convertColumn(cleanInput.charAt(2));
            int toRow = 8 - Character.getNumericValue(cleanInput.charAt(3));
            
            return board.makeMove(fromRow, fromCol, toRow, toCol, currentPlayer);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    private int convertColumn(char colChar) {
        char lower = Character.toLowerCase(colChar);
        if (lower < 'a' || lower > 'h') {
            throw new IllegalArgumentException("Cột phải từ a đến h");
        }
        return lower - 'a';
    }
    
    private void checkGameEnd() {
        if (board.isCheckmate(PieceColor.WHITE)) {
            gameOver = true;
            winner = "BLACK";
        } else if (board.isCheckmate(PieceColor.BLACK)) {
            gameOver = true;
            winner = "WHITE";
        } else if (board.isDraw()) {
            gameOver = true;
            winner = "DRAW";
        }
    }
    
    private void resign() {
        gameOver = true;
        winner = (currentPlayer == PieceColor.WHITE) ? "BLACK" : "WHITE";
        System.out.println(currentPlayer + " đã đầu hàng!");
    }
    
    private void undoMove() {
        if (board.getMoveHistory().isEmpty()) {
            System.out.println("Không có nước đi nào để hoàn tác!");
            return;
        }
        
        if (board.undoLastMove()) {
            currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                            PieceColor.BLACK : PieceColor.WHITE;
            System.out.println("Đã hoàn tác nước đi cuối.");
            board.printBoard();
        }
    }
    
    private void showLegalMoves() {
        System.out.println("\nCác nước đi hợp lệ cho " + currentPlayer + ":");
        List<Move> legalMoves = board.getLegalMoves(currentPlayer);
        
        if (legalMoves.isEmpty()) {
            System.out.println("Không có nước đi hợp lệ nào!");
            return;
        }
        
        for (Move move : legalMoves) {
            char fromColChar = (char)('a' + move.fromCol);
            char toColChar = (char)('a' + move.toCol);
            System.out.printf("  %s %c%d → %c%d%n",
                move.moved.getSymbol(),
                fromColChar, (8 - move.fromRow),
                toColChar, (8 - move.toRow));
        }
        
        System.out.println("Tổng cộng: " + legalMoves.size() + " nước đi");
        System.out.println();
    }
    
    private void printGameState() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("Lượt: " + currentPlayer);
        
        if (board.isInCheck(PieceColor.WHITE)) {
            System.out.println("⚠️  VUA TRẮNG ĐANG BỊ CHIẾU!");
        }
        if (board.isInCheck(PieceColor.BLACK)) {
            System.out.println("⚠️  VUA ĐEN ĐANG BỊ CHIẾU!");
        }
        
        board.printBoard();
    }
    
    private void printGameResult() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("TRẬN ĐẤU KẾT THÚC!");
        
        if (winner.equals("DRAW")) {
            System.out.println("KẾT QUẢ: HÒA!");
            System.out.println("Lý do: Hết nước đi (Stalemate)");
        } else {
            System.out.println("🎉 NGƯỜI CHIẾN THẮNG: " + winner + "!");
            
            if (board.isCheckmate(PieceColor.WHITE) || board.isCheckmate(PieceColor.BLACK)) {
                System.out.println("Chiến thắng bằng: CHIẾU HẾT!");
            }
        }
        
        System.out.println("Tổng số nước đi: " + board.getMoveHistory().size());
        System.out.println("=".repeat(40));
    }
    
    public Board getBoard() {
        return board;
    }
    
    public PieceColor getCurrentPlayer() {
        return currentPlayer;
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public String getWinner() {
        return winner;
    }
    
    public static void main(String[] args) {
        System.out.println("=== CHESS GAME LAUNCHER ===");
        System.out.println("1. Chơi với người");
        System.out.println("2. Test các nước đi");
        System.out.println("3. Thoát");
        System.out.print("Chọn: ");
        
        java.util.Scanner scanner = new java.util.Scanner(System.in);
        int choice = scanner.nextInt();
        
        switch (choice) {
            case 1:
                GameTest game = new GameTest();
                game.start();
                break;
            case 2:
                runTests();
                break;
            case 3:
                System.out.println("Tạm biệt!");
                break;
            default:
                System.out.println("Lựa chọn không hợp lệ!");
        }
        
        scanner.close();
    }
    
    private static void runTests() {
        System.out.println("\n=== TESTING MODE ===");
        
        model.Board board = new model.Board();
        GameTest game = new GameTest();
        
        System.out.println("\n1. Test nước đi hợp lệ:");
        System.out.println("   e2 e4: " + board.isValidMove(6, 4, 4, 4, model.PieceColor.WHITE));
        System.out.println("   g1 f3: " + board.isValidMove(7, 6, 5, 5, model.PieceColor.WHITE));
        
        System.out.println("\n2. Test nước đi không hợp lệ:");
        System.out.println("   e2 e5: " + board.isValidMove(6, 4, 3, 4, model.PieceColor.WHITE));
        System.out.println("   a1 a3: " + board.isValidMove(7, 0, 5, 0, model.PieceColor.WHITE));
        
        System.out.println("\n3. Test chiếu hết nhanh:");
        testScholarMate();
        
        System.out.println("\n=== TESTS COMPLETED ===");
    }
    
    private static void testScholarMate() {
        model.Board board = new model.Board();
        
        System.out.println("   e2 e4: " + board.makeMove(6, 4, 4, 4, model.PieceColor.WHITE));
        System.out.println("   e7 e5: " + board.makeMove(1, 4, 3, 4, model.PieceColor.BLACK));
        System.out.println("   d1 h5: " + board.makeMove(7, 3, 3, 7, model.PieceColor.WHITE));
        System.out.println("   b8 c6: " + board.makeMove(0, 1, 2, 2, model.PieceColor.BLACK));
        System.out.println("   f1 c4: " + board.makeMove(7, 5, 4, 2, model.PieceColor.WHITE));
        System.out.println("   g8 f6: " + board.makeMove(0, 6, 2, 5, model.PieceColor.BLACK));
        System.out.println("   h5 f7#: " + board.makeMove(3, 7, 1, 5, model.PieceColor.WHITE));
        
        System.out.println("   Chiếu hết? " + board.isCheckmate(model.PieceColor.BLACK));
    }
}