package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import model.*;
import modelAI.ChessAI;

public class ChessUI extends JFrame {
    private Board board;
    private PieceColor currentPlayer;
    private ChessSquare[][] squares;
    private Position selectedPosition;
    private JLabel statusLabel;
    private JPanel boardPanel;
    private JTextArea moveHistoryArea;
    
    private final Color lightColor = new Color(240, 217, 181);
    private final Color darkColor = new Color(181, 136, 99);
    private final Color HIGHLIGHT_COLOR = new Color(255, 255, 100, 150);
    private final Color SELECTED_COLOR = new Color(100, 200, 255, 150);
    
    private final Color whitePieceColor = Color.WHITE;
    private final Color blackPieceColor = Color.BLACK;
    private final Color whitePieceOutline = Color.LIGHT_GRAY;
    private final Color blackPieceOutline = Color.DARK_GRAY;
    
    private int selectedPromotion = -1;
    private JDialog promotionDialog;
    private static final int QUEEN = 0;
    private static final int ROOK = 1;
    private static final int BISHOP = 2;
    private static final int KNIGHT = 3;
    
    // AI
    private ChessAI chessAI;
    private boolean vsAI = true;
    private PieceColor aiColor = PieceColor.BLACK;
    private Timer aiTimer;
    
    // Độ khó AI
    private int aiDifficulty = 3;
    
    public ChessUI() {
        board = new Board();
        currentPlayer = PieceColor.WHITE;
        selectedPosition = null;
        
        chessAI = new ChessAI(aiColor, aiDifficulty);
        
        initializeUI();
        updateBoard();
        
        if (vsAI && aiColor == PieceColor.WHITE) {
            currentPlayer = aiColor;
            updateStatus();
            makeAIMove();
        }
    }
    
    private void initializeUI() {
        setTitle("Java Chess Game - Chơi với Máy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel bàn cờ
        boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(600, 600));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        squares = new ChessSquare[8][8];
        
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = new ChessSquare(row, col);
                boardPanel.add(squares[row][col]);
            }
        }

        JPanel infoPanel = createInfoPanel();
        
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        createMenuBar();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private class ChessSquare extends JPanel {
        private final int row;
        private final int col;
        private boolean isSelected = false;
        private boolean isHighlighted = false;
        private String pieceSymbol = "";
        private Color pieceColor = null;
        private Color pieceOutline = null;
        
        public ChessSquare(int row, int col) {
            this.row = row;
            this.col = col;
            setOpaque(true);
            setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
            
            setBackground((row + col) % 2 == 0 ? lightColor : darkColor);
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    handleSquareClick(row, col);
                }
            });
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                                RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (isSelected) {
                g2d.setColor(SELECTED_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else if (isHighlighted) {
                g2d.setColor(HIGHLIGHT_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
            if (!pieceSymbol.isEmpty()) {
                Font font = new Font("Arial Unicode MS", Font.BOLD, 48);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(pieceSymbol)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                if (pieceOutline != null) {
                    g2d.setColor(pieceOutline);
                    g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            if (dx != 0 || dy != 0) {
                                g2d.drawString(pieceSymbol, x + dx, y + dy);
                            }
                        }
                    }
                }
                
                if (pieceColor != null) {
                    g2d.setColor(pieceColor);
                }
                g2d.drawString(pieceSymbol, x, y);
            }
        }
        
        public void setPiece(String symbol, Color color, Color outline) {
            this.pieceSymbol = symbol;
            this.pieceColor = color;
            this.pieceOutline = outline;
            repaint();
        }
        
        public void clearPiece() {
            this.pieceSymbol = "";
            this.pieceColor = null;
            this.pieceOutline = null;
            repaint();
        }
        
        public void setSelected(boolean selected) {
            this.isSelected = selected;
            repaint();
        }
        
        public void setHighlighted(boolean highlighted) {
            this.isHighlighted = highlighted;
            repaint();
        }
        
        public void updateBackground() {
            setBackground((row + col) % 2 == 0 ? lightColor : darkColor);
            repaint();
        }
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setPreferredSize(new Dimension(200, 600));
        
        statusLabel = new JLabel("Lượt: TRẮNG (BẠN)");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel aiLabel = new JLabel("AI: ĐEN - Cấp " + aiDifficulty);
        aiLabel.setFont(new Font("Arial", Font.BOLD, 14));
        aiLabel.setForeground(Color.BLUE);
        aiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton newGameBtn = createButton("Trận mới", e -> newGame());
        JButton undoBtn = createButton("Hoàn tác", e -> undoMove());
        JButton resignBtn = createButton("Đầu hàng", e -> resign());
        JButton aiSettingsBtn = createButton("Cài đặt AI", e -> showAISettings());
        
        JLabel historyLabel = new JLabel("Lịch sử nước đi:");
        historyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        moveHistoryArea = new JTextArea(10, 15);
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(aiLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(newGameBtn);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(undoBtn);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(resignBtn);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(aiSettingsBtn);
        infoPanel.add(Box.createVerticalStrut(20));
        infoPanel.add(historyLabel);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(scrollPane);
        
        return infoPanel;
    }
    
    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(180, 30));
        button.addActionListener(listener);
        return button;
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu gameMenu = new JMenu("Trò chơi");
        JMenuItem newGameItem = new JMenuItem("Trận mới");
        JMenuItem undoItem = new JMenuItem("Hoàn tác");
        JMenuItem resignItem = new JMenuItem("Đầu hàng");
        JMenuItem aiSettingsItem = new JMenuItem("Cài đặt AI...");
        JMenuItem exitItem = new JMenuItem("Thoát");
        
        newGameItem.addActionListener(e -> newGame());
        undoItem.addActionListener(e -> undoMove());
        resignItem.addActionListener(e -> resign());
        aiSettingsItem.addActionListener(e -> showAISettings());
        exitItem.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newGameItem);
        gameMenu.add(undoItem);
        gameMenu.add(resignItem);
        gameMenu.addSeparator();
        gameMenu.add(aiSettingsItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        menuBar.add(gameMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void handleSquareClick(int row, int col) {
        if (vsAI && currentPlayer == aiColor) {
            return;
        }
        
        if (selectedPosition == null) {
            Piece piece = board.getPiece(row, col);
            if (piece != null && piece.getColor() == currentPlayer) {
                selectedPosition = new Position(row, col);
                squares[row][col].setSelected(true);
                
                highlightLegalMoves(row, col);
            }
        } else {
            int fromRow = selectedPosition.row;
            int fromCol = selectedPosition.col;
            
            Piece clickedPiece = board.getPiece(row, col);
            if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
                clearHighlights();
                selectedPosition = new Position(row, col);
                squares[row][col].setSelected(true);
                highlightLegalMoves(row, col);
                return;
            }
            
            Piece selectedPiece = board.getPiece(fromRow, fromCol);
            boolean isCastlingMove = (selectedPiece instanceof King && Math.abs(fromCol - col) == 2);
            
            boolean moveSuccess = board.makeMove(fromRow, fromCol, row, col, currentPlayer);
            
            if (moveSuccess) {
                handlePromotionIfNeeded(row, col);
                
                addMoveToHistory(fromRow, fromCol, row, col, isCastlingMove, true);
                
                checkGameEnd();
                
                if (!board.isCheckmate(currentPlayer) && !board.isDraw()) {
                    clearHighlights();
                    selectedPosition = null;
                    updateBoard();
                    
                    if (vsAI && !board.isCheckmate(currentPlayer) && !board.isDraw()) {
                        currentPlayer = aiColor;
                        updateStatus();
                        
                        if (aiTimer != null && aiTimer.isRunning()) {
                            aiTimer.stop();
                        }
                        aiTimer = new Timer(500, e -> {
                            makeAIMove();
                            ((Timer)e.getSource()).stop();
                        });
                        aiTimer.setRepeats(false);
                        aiTimer.start();
                    } else if (!vsAI) {
                        currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                                        PieceColor.BLACK : PieceColor.WHITE;
                        updateStatus();
                        updateBoard();
                    }
                } else {
                    clearHighlights();
                    selectedPosition = null;
                    updateBoard();
                }
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Nước đi không hợp lệ!", 
                    "Lỗi", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void makeAIMove() {
        if (!vsAI || currentPlayer != aiColor || 
            board.isCheckmate(aiColor) || board.isDraw()) {
            return;
        }
        
        statusLabel.setText("MÁY đang suy nghĩ...");
        
        new Thread(() -> {
            Move aiMove = chessAI.getBestMove(board, currentPlayer);
            
            SwingUtilities.invokeLater(() -> {
                if (aiMove != null) {
                    boolean moveSuccess = board.makeMove(
                        aiMove.fromRow, aiMove.fromCol, 
                        aiMove.toRow, aiMove.toCol, 
                        currentPlayer);
                    
                    if (moveSuccess) {
                        handleAIPromotionIfNeeded(aiMove.toRow, aiMove.toCol);
                        
                        boolean isCastling = (aiMove.moved instanceof King && 
                                            Math.abs(aiMove.fromCol - aiMove.toCol) == 2);
                        addMoveToHistory(aiMove.fromRow, aiMove.fromCol, 
                                       aiMove.toRow, aiMove.toCol, 
                                       isCastling, false);
                        
                        checkGameEnd();
                        
                        if (!board.isCheckmate(currentPlayer) && !board.isDraw()) {
                            currentPlayer = (aiColor == PieceColor.WHITE) ? 
                                            PieceColor.BLACK : PieceColor.WHITE;
                            updateStatus();
                            
                            updateBoard();
                        } else {
                            updateBoard();
                        }
                    } else {
                        JOptionPane.showMessageDialog(ChessUI.this,
                            "Máy đưa ra nước đi không hợp lệ!",
                            "Lỗi AI",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(ChessUI.this,
                        "Máy không tìm được nước đi hợp lệ!",
                        "Lỗi AI",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }
    
    private void handleAIPromotionIfNeeded(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece instanceof Pawn) {
            int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
            if (row == promotionRow) {
                Piece promotedPiece = new Queen(piece.getColor(), row, col);
                board.setPiece(row, col, promotedPiece);
                promotedPiece.setHasMoved(true);
                
                moveHistoryArea.append("   → MÁY phong cấp thành Hậu\n");
            }
        }
    }
    
    private void handlePromotionIfNeeded(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece instanceof Pawn) {
            int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
            if (row == promotionRow) {
                showPromotionDialog(row, col, piece.getColor());
            }
        }
    }
    
    private void showPromotionDialog(int row, int col, PieceColor color) {
        String[] options = {"Hậu", "Xe", "Tượng", "Mã"};
        String[] symbols = color == PieceColor.WHITE ? 
            new String[]{"♕", "♖", "♗", "♘"} : 
            new String[]{"♛", "♜", "♝", "♞"};
        
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton[] buttons = new JButton[4];
        
        for (int i = 0; i < 4; i++) {
            buttons[i] = new JButton(symbols[i] + " " + options[i]);
            buttons[i].setFont(new Font("Arial Unicode MS", Font.BOLD, 20));
            buttons[i].setPreferredSize(new Dimension(150, 60));
            final int choice = i;
            buttons[i].addActionListener(e -> {
                selectedPromotion = choice;
                applyPromotion(row, col, color, choice);
                if (promotionDialog != null) {
                    promotionDialog.dispose();
                    promotionDialog = null;
                }
            });
            panel.add(buttons[i]);
        }
        
        promotionDialog = new JDialog(this, "Chọn quân phong cấp", true);
        promotionDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        promotionDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                selectedPromotion = QUEEN;
                applyPromotion(row, col, color, QUEEN);
                promotionDialog.dispose();
                promotionDialog = null;
            }
        });
        
        promotionDialog.add(panel);
        promotionDialog.pack();
        promotionDialog.setLocationRelativeTo(this);
        promotionDialog.setVisible(true);
    }
    
    private void applyPromotion(int row, int col, PieceColor color, int promotionType) {
        Piece promotedPiece = null;
        
        switch (promotionType) {
            case QUEEN:
                promotedPiece = new Queen(color, row, col);
                break;
            case ROOK:
                promotedPiece = new Rook(color, row, col);
                break;
            case BISHOP:
                promotedPiece = new Bishop(color, row, col);
                break;
            case KNIGHT:
                promotedPiece = new Knight(color, row, col);
                break;
        }
        
        if (promotedPiece != null) {
            board.setPiece(row, col, promotedPiece);
            promotedPiece.setHasMoved(true);
            
            updateBoard();
            
            String pieceName = "";
            switch (promotionType) {
                case QUEEN: pieceName = "Hậu"; break;
                case ROOK: pieceName = "Xe"; break;
                case BISHOP: pieceName = "Tượng"; break;
                case KNIGHT: pieceName = "Mã"; break;
            }
            moveHistoryArea.append("   → BẠN phong cấp thành " + pieceName + "\n");
        }
    }
    
    private void addMoveToHistory(int fromRow, int fromCol, int toRow, int toCol, 
                                  boolean isCastling, boolean isPlayer) {
        char fromColChar = (char)('a' + fromCol);
        char toColChar = (char)('a' + toCol);
        
        String moveNotation;
        if (isCastling) {
            boolean kingside = (toCol > fromCol);
            moveNotation = kingside ? "O-O (Nhập thành ngắn)" : "O-O-O (Nhập thành dài)";
        } else {
            moveNotation = String.format("%c%d → %c%d",
                fromColChar, (8 - fromRow),
                toColChar, (8 - toRow));
        }
        
        String prefix = isPlayer ? "BẠN:  " : "MÁY: ";
        moveHistoryArea.append(prefix + moveNotation + "\n");
        moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());
    }
    
    private void highlightLegalMoves(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece == null) return;
        
        List<Position> moves = piece.getPossibleMoves(board);
        for (Position move : moves) {
            if (board.isValidMove(row, col, move.row, move.col, currentPlayer)) {
                squares[move.row][move.col].setHighlighted(true);
            }
        }
    }
    
    private void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setSelected(false);
                squares[row][col].setHighlighted(false);
                squares[row][col].updateBackground();
            }
        }
    }
    
    private void updateBoard() {
        SwingUtilities.invokeLater(() -> {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    Piece piece = board.getPiece(row, col);
                    ChessSquare square = squares[row][col];
                    
                    if (piece != null) {
                        char symbol = piece.getSymbol();
                        String unicode = getUnicodeChar(symbol);
                        
                        if (piece.getColor() == PieceColor.WHITE) {
                            square.setPiece(unicode, whitePieceColor, whitePieceOutline);
                        } else {
                            square.setPiece(unicode, blackPieceColor, blackPieceOutline);
                        }
                    } else {
                        square.clearPiece();
                    }
                }
            }
            
            if (board.isInCheck(PieceColor.WHITE)) {
                if (currentPlayer == PieceColor.WHITE) {
                    statusLabel.setText("TRẮNG (BẠN) - ĐANG BỊ CHIẾU!");
                } else {
                    statusLabel.setText("TRẮNG (MÁY) - ĐANG BỊ CHIẾU!");
                }
                statusLabel.setForeground(Color.RED);
            } else if (board.isInCheck(PieceColor.BLACK)) {
                if (currentPlayer == PieceColor.BLACK) {
                    statusLabel.setText("ĐEN (BẠN) - ĐANG BỊ CHIẾU!");
                } else {
                    statusLabel.setText("ĐEN (MÁY) - ĐANG BỊ CHIẾU!");
                }
                statusLabel.setForeground(Color.RED);
            } else {
                updateStatus();
                statusLabel.setForeground(Color.BLACK);
            }
            
            boardPanel.repaint();
        });
    }
    
    private String getUnicodeChar(char symbol) {
        switch (Character.toLowerCase(symbol)) {
            case 'k': return symbol == 'K' ? "♔" : "♚";
            case 'q': return symbol == 'Q' ? "♕" : "♛";
            case 'r': return symbol == 'R' ? "♖" : "♜";
            case 'b': return symbol == 'B' ? "♗" : "♝";
            case 'n': return symbol == 'N' ? "♘" : "♞";
            case 'p': return symbol == 'P' ? "♙" : "♟";
            default: return "";
        }
    }
    
    private void updateStatus() {
        if (vsAI) {
            if (currentPlayer == PieceColor.WHITE) {
                if (aiColor == PieceColor.WHITE) {
                    statusLabel.setText("Lượt: TRẮNG (MÁY)");
                } else {
                    statusLabel.setText("Lượt: TRẮNG (BẠN)");
                }
            } else {
                if (aiColor == PieceColor.BLACK) {
                    statusLabel.setText("Lượt: ĐEN (MÁY)");
                } else {
                    statusLabel.setText("Lượt: ĐEN (BẠN)");
                }
            }
        } else {
            if (currentPlayer == PieceColor.WHITE) {
                statusLabel.setText("Lượt: TRẮNG");
            } else {
                statusLabel.setText("Lượt: ĐEN");
            }
        }
    }
    
    private void checkGameEnd() {
        if (board.isCheckmate(PieceColor.WHITE)) {
            String winner = (aiColor == PieceColor.BLACK && vsAI) ? "MÁY" : "ĐEN";
            JOptionPane.showMessageDialog(this,
                "CHIẾU HẾT!\n" + winner + " THẮNG!",
                "Trận đấu kết thúc",
                JOptionPane.INFORMATION_MESSAGE);
            disableBoard();
        } else if (board.isCheckmate(PieceColor.BLACK)) {
            String winner = (aiColor == PieceColor.WHITE && vsAI) ? "MÁY" : "TRẮNG";
            JOptionPane.showMessageDialog(this,
                "CHIẾU HẾT!\n" + winner + " THẮNG!",
                "Trận đấu kết thúc",
                JOptionPane.INFORMATION_MESSAGE);
            disableBoard();
        } else if (board.isDraw()) {
            String drawReason = "HÒA!\n";
            if (board.isStalemate(PieceColor.WHITE) || board.isStalemate(PieceColor.BLACK)) {
                drawReason += "Lý do: Hết nước đi hợp lệ (Stalemate)";
            } else if (board.getHalfMoveClock() >= 100) {
                drawReason += "Lý do: 50 nước không ăn quân";
            } else {
                drawReason += "Lý do: Cờ hòa";
            }
            
            JOptionPane.showMessageDialog(this,
                drawReason,
                "Trận đấu kết thúc",
                JOptionPane.INFORMATION_MESSAGE);
            disableBoard();
        }
    }
    
    private void disableBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setEnabled(false);
            }
        }
        if (aiTimer != null && aiTimer.isRunning()) {
            aiTimer.stop();
        }
    }
    
    private void newGame() {
        int result = JOptionPane.showConfirmDialog(this,
            "Bắt đầu trận mới? Lịch sử nước đi sẽ bị xóa.",
            "Trận mới",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            board = new Board();
            currentPlayer = PieceColor.WHITE;
            selectedPosition = null;
            moveHistoryArea.setText("");
            
            if (vsAI && aiColor == PieceColor.WHITE) {
                currentPlayer = aiColor;
                makeAIMove();
            }
            
            updateStatus();
            clearHighlights();
            updateBoard();
            enableBoard();
        }
    }
    
    private void enableBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setEnabled(true);
            }
        }
    }
    
    private void undoMove() {
        if (vsAI && currentPlayer == aiColor) {
            JOptionPane.showMessageDialog(this,
                "Không thể hoàn tác khi đang đến lượt máy!",
                "Hoàn tác",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (vsAI && currentPlayer == PieceColor.WHITE && !board.getMoveHistory().isEmpty()) {
            if (board.undoLastMove()) {
                if (board.undoLastMove()) {
                    currentPlayer = PieceColor.WHITE;
                    updateStatus();
                    clearHighlights();
                    updateBoard();
                    
                    String text = moveHistoryArea.getText();
                    if (!text.isEmpty()) {
                        String[] lines = text.split("\n");
                        StringBuilder newText = new StringBuilder();
                        int linesToKeep = Math.max(0, lines.length - 2);
                        for (int i = 0; i < linesToKeep; i++) {
                            newText.append(lines[i]).append("\n");
                        }
                        moveHistoryArea.setText(newText.toString());
                    }
                }
            }
        } 
        else if (board.undoLastMove()) {
            currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                            PieceColor.BLACK : PieceColor.WHITE;
            updateStatus();
            clearHighlights();
            updateBoard();
            
            String text = moveHistoryArea.getText();
            if (!text.isEmpty()) {
                String[] lines = text.split("\n");
                StringBuilder newText = new StringBuilder();
                int linesToKeep = Math.max(0, lines.length - 1);
                for (int i = 0; i < linesToKeep; i++) {
                    newText.append(lines[i]).append("\n");
                }
                moveHistoryArea.setText(newText.toString());
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Không có nước đi nào để hoàn tác!",
                "Hoàn tác",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void resign() {
        String winner, resigner;
        
        if (vsAI) {
            if (currentPlayer == PieceColor.WHITE) {
                if (aiColor == PieceColor.WHITE) {
                    winner = "ĐEN (BẠN)";
                    resigner = "TRẮNG (MÁY)";
                } else {
                    winner = "ĐEN (MÁY)";
                    resigner = "TRẮNG (BẠN)";
                }
            } else {
                if (aiColor == PieceColor.BLACK) {
                    winner = "TRẮNG (BẠN)";
                    resigner = "ĐEN (MÁY)";
                } else {
                    winner = "TRẮNG (MÁY)";
                    resigner = "ĐEN (BẠN)";
                }
            }
        } else {
            winner = (currentPlayer == PieceColor.WHITE) ? "ĐEN" : "TRẮNG";
            resigner = (currentPlayer == PieceColor.WHITE) ? "TRẮNG" : "ĐEN";
        }
        
        int result = JOptionPane.showConfirmDialog(this,
            resigner + " có chắc muốn đầu hàng? " + winner + " sẽ thắng.",
            "Đầu hàng",
            JOptionPane.YES_NO_OPTION);
            
        if (result == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(this,
                winner + " THẮNG!\n" + resigner + " đã đầu hàng.",
                "Trận đấu kết thúc",
                JOptionPane.INFORMATION_MESSAGE);
            disableBoard();
        }
    }
    
    private void showAISettings() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel depthLabel = new JLabel("Độ khó AI (1-5):");
        JComboBox<Integer> depthCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        depthCombo.setSelectedItem(aiDifficulty);
        
        JLabel colorLabel = new JLabel("Máy chơi:");
        JComboBox<String> colorCombo = new JComboBox<>(new String[]{"ĐEN", "TRẮNG"});
        colorCombo.setSelectedItem(aiColor == PieceColor.BLACK ? "ĐEN" : "TRẮNG");
        
        panel.add(depthLabel);
        panel.add(depthCombo);
        panel.add(colorLabel);
        panel.add(colorCombo);
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Cài đặt AI", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            aiDifficulty = (Integer) depthCombo.getSelectedItem();
            String selectedColor = (String) colorCombo.getSelectedItem();
            PieceColor newAiColor = selectedColor.equals("ĐEN") ? PieceColor.BLACK : PieceColor.WHITE;
            
            boolean colorChanged = (newAiColor != aiColor);
            boolean difficultyChanged = (aiDifficulty != (Integer) depthCombo.getSelectedItem());
            
            if (colorChanged || difficultyChanged) {
                aiColor = newAiColor;
                
                chessAI = new ChessAI(aiColor, aiDifficulty);
                
                if (colorChanged) {
                    if (aiColor == PieceColor.WHITE) {
                        currentPlayer = PieceColor.BLACK;
                    } else {
                        currentPlayer = PieceColor.WHITE;
                    }
                }
                
                updateStatus();
                updateBoard();
                
                JOptionPane.showMessageDialog(this,
                    "Đã cập nhật cài đặt AI:\n" +
                    "Độ khó: Cấp " + aiDifficulty + "\n" +
                    "Máy chơi: " + (aiColor == PieceColor.WHITE ? "TRẮNG" : "ĐEN"),
                    "Cài đặt AI",
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (colorChanged && vsAI && currentPlayer == aiColor) {
                    makeAIMove();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new ChessUI();
        });
    }
}