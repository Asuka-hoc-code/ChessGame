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
    
    // Màu sắc cố định cho bàn cờ
    private final Color lightColor = new Color(240, 217, 181);
    private final Color darkColor = new Color(181, 136, 99);
    private final Color HIGHLIGHT_COLOR = new Color(255, 255, 100, 150);
    private final Color SELECTED_COLOR = new Color(100, 200, 255, 150);
    
    // Màu sắc cố định cho quân cờ
    private final Color whitePieceColor = Color.WHITE;
    private final Color blackPieceColor = Color.BLACK;
    private final Color whitePieceOutline = Color.LIGHT_GRAY;
    private final Color blackPieceOutline = Color.DARK_GRAY;
    
    // Biến cho phong cấp
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
        
        // Khởi tạo AI với độ khó mặc định
        chessAI = new ChessAI(aiColor, aiDifficulty);
        
        initializeUI();
        updateBoard();
        
        // Nếu AI chơi trắng, cho AI đi trước
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
        
        // Panel chính
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel bàn cờ
        boardPanel = new JPanel(new GridLayout(8, 8));
        boardPanel.setPreferredSize(new Dimension(600, 600));
        boardPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        
        squares = new ChessSquare[8][8];
        
        // Tạo các ô cờ
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = new ChessSquare(row, col);
                boardPanel.add(squares[row][col]);
            }
        }
        
        // Panel thông tin bên phải
        JPanel infoPanel = createInfoPanel();
        
        // Thêm các panel vào frame
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        mainPanel.add(infoPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Menu
        createMenuBar();
        
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    // Lớp ChessSquare tùy chỉnh để hiển thị ô cờ
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
            
            // Đặt màu nền xen kẽ
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
            
            // Vẽ nền nếu được chọn hoặc highlight
            if (isSelected) {
                g2d.setColor(SELECTED_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            } else if (isHighlighted) {
                g2d.setColor(HIGHLIGHT_COLOR);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
            
            // Vẽ quân cờ nếu có
            if (!pieceSymbol.isEmpty()) {
                Font font = new Font("Arial Unicode MS", Font.BOLD, 48);
                g2d.setFont(font);
                FontMetrics fm = g2d.getFontMetrics();
                
                int x = (getWidth() - fm.stringWidth(pieceSymbol)) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                
                // Vẽ outline
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
                
                // Vẽ chữ chính
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
        
        // Label trạng thái
        statusLabel = new JLabel("Lượt: TRẮNG (BẠN)");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Label AI
        JLabel aiLabel = new JLabel("AI: ĐEN - Cấp " + aiDifficulty);
        aiLabel.setFont(new Font("Arial", Font.BOLD, 14));
        aiLabel.setForeground(Color.BLUE);
        aiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nút chức năng
        JButton newGameBtn = createButton("Trận mới", e -> newGame());
        JButton undoBtn = createButton("Hoàn tác", e -> undoMove());
        JButton resignBtn = createButton("Đầu hàng", e -> resign());
        JButton aiSettingsBtn = createButton("Cài đặt AI", e -> showAISettings());
        
        // Khu vực lịch sử nước đi
        JLabel historyLabel = new JLabel("Lịch sử nước đi:");
        historyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        moveHistoryArea = new JTextArea(10, 15);
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        
        // Thêm các thành phần vào infoPanel
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
        // Nếu đang đến lượt AI (máy), bỏ qua click
        if (vsAI && currentPlayer == aiColor) {
            return;
        }
        
        // Nếu chưa chọn quân nào
        if (selectedPosition == null) {
            Piece piece = board.getPiece(row, col);
            if (piece != null && piece.getColor() == currentPlayer) {
                // Chọn quân cờ
                selectedPosition = new Position(row, col);
                squares[row][col].setSelected(true);
                
                // Hiển thị các nước đi hợp lệ
                highlightLegalMoves(row, col);
            }
        } else {
            // Đã chọn quân, giờ chọn ô đích
            int fromRow = selectedPosition.row;
            int fromCol = selectedPosition.col;
            
            // Kiểm tra xem có click vào quân cùng màu khác không
            Piece clickedPiece = board.getPiece(row, col);
            if (clickedPiece != null && clickedPiece.getColor() == currentPlayer) {
                // Chọn quân mới
                clearHighlights();
                selectedPosition = new Position(row, col);
                squares[row][col].setSelected(true);
                highlightLegalMoves(row, col);
                return;
            }
            
            // Kiểm tra xem có phải nước đi nhập thành không
            Piece selectedPiece = board.getPiece(fromRow, fromCol);
            boolean isCastlingMove = (selectedPiece instanceof King && Math.abs(fromCol - col) == 2);
            
            boolean moveSuccess = board.makeMove(fromRow, fromCol, row, col, currentPlayer);
            
            if (moveSuccess) {
                // Kiểm tra xem có cần phong cấp không
                handlePromotionIfNeeded(row, col);
                
                // Thêm vào lịch sử với ký hiệu "BẠN"
                addMoveToHistory(fromRow, fromCol, row, col, isCastlingMove, true);
                
                // Kiểm tra kết thúc trò chơi
                checkGameEnd();
                
                if (!board.isCheckmate(currentPlayer) && !board.isDraw()) {
                    // Xóa selection và highlight
                    clearHighlights();
                    selectedPosition = null;
                    updateBoard();
                    
                    // Nếu chơi với AI và chưa kết thúc, để AI đi
                    if (vsAI && !board.isCheckmate(currentPlayer) && !board.isDraw()) {
                        // Đổi lượt sang AI
                        currentPlayer = aiColor;
                        updateStatus();
                        
                        // Cho AI đi sau 500ms
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
                        // Nếu không chơi với AI, đổi lượt bình thường
                        currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                                        PieceColor.BLACK : PieceColor.WHITE;
                        updateStatus();
                        updateBoard();
                    }
                } else {
                    // Trò chơi kết thúc
                    clearHighlights();
                    selectedPosition = null;
                    updateBoard();
                }
            } else {
                // Nước đi không hợp lệ
                JOptionPane.showMessageDialog(this, 
                    "Nước đi không hợp lệ!", 
                    "Lỗi", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    private void makeAIMove() {
        // Kiểm tra điều kiện trước khi AI đi
        if (!vsAI || currentPlayer != aiColor || 
            board.isCheckmate(aiColor) || board.isDraw()) {
            return;
        }
        
        // Hiển thị trạng thái AI đang suy nghĩ
        statusLabel.setText("MÁY đang suy nghĩ...");
        
        // Tạo thread riêng để AI tính toán
        new Thread(() -> {
            Move aiMove = chessAI.getBestMove(board, currentPlayer);
            
            // Thực hiện nước đi trên EDT
            SwingUtilities.invokeLater(() -> {
                if (aiMove != null) {
                    // Thực hiện nước đi của AI
                    boolean moveSuccess = board.makeMove(
                        aiMove.fromRow, aiMove.fromCol, 
                        aiMove.toRow, aiMove.toCol, 
                        currentPlayer);
                    
                    if (moveSuccess) {
                        // AI tự động phong cấp thành Hậu nếu cần
                        handleAIPromotionIfNeeded(aiMove.toRow, aiMove.toCol);
                        
                        // Thêm vào lịch sử với ký hiệu "MÁY"
                        boolean isCastling = (aiMove.moved instanceof King && 
                                            Math.abs(aiMove.fromCol - aiMove.toCol) == 2);
                        addMoveToHistory(aiMove.fromRow, aiMove.fromCol, 
                                       aiMove.toRow, aiMove.toCol, 
                                       isCastling, false);
                        
                        // Kiểm tra kết thúc trò chơi
                        checkGameEnd();
                        
                        if (!board.isCheckmate(currentPlayer) && !board.isDraw()) {
                            // Đổi lượt về người chơi
                            currentPlayer = (aiColor == PieceColor.WHITE) ? 
                                            PieceColor.BLACK : PieceColor.WHITE;
                            updateStatus();
                            
                            // Cập nhật bàn cờ
                            updateBoard();
                        } else {
                            // Trò chơi kết thúc
                            updateBoard();
                        }
                    } else {
                        // AI đưa ra nước đi không hợp lệ
                        JOptionPane.showMessageDialog(ChessUI.this,
                            "Máy đưa ra nước đi không hợp lệ!",
                            "Lỗi AI",
                            JOptionPane.WARNING_MESSAGE);
                    }
                } else {
                    // AI không tìm được nước đi
                    JOptionPane.showMessageDialog(ChessUI.this,
                        "Máy không tìm được nước đi hợp lệ!",
                        "Lỗi AI",
                        JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }
    
    // Xử lý phong cấp cho AI (luôn chọn Hậu)
    private void handleAIPromotionIfNeeded(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece instanceof Pawn) {
            int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
            if (row == promotionRow) {
                // AI luôn phong cấp thành Hậu
                Piece promotedPiece = new Queen(piece.getColor(), row, col);
                board.setPiece(row, col, promotedPiece);
                promotedPiece.setHasMoved(true);
                
                // Thêm thông tin vào lịch sử
                moveHistoryArea.append("   → MÁY phong cấp thành Hậu\n");
            }
        }
    }
    
    // Xử lý phong cấp cho người chơi
    private void handlePromotionIfNeeded(int row, int col) {
        Piece piece = board.getPiece(row, col);
        if (piece instanceof Pawn) {
            int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
            if (row == promotionRow) {
                // Hiển thị dialog chọn quân phong cấp
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
        
        // Xử lý đóng dialog (mặc định chọn Hậu)
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
            
            // Cập nhật lại giao diện
            updateBoard();
            
            // Thêm thông tin phong cấp vào lịch sử
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
                        // Sử dụng Unicode cho các quân cờ
                        char symbol = piece.getSymbol();
                        String unicode = getUnicodeChar(symbol);
                        
                        // Đặt màu cho quân cờ
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
            
            // Hiển thị cảnh báo chiếu
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
            
            // Cập nhật lại giao diện
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
            
            // Nếu AI chơi trắng, cho AI đi trước
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
        // Không cho phép hoàn tác khi đang đến lượt AI
        if (vsAI && currentPlayer == aiColor) {
            JOptionPane.showMessageDialog(this,
                "Không thể hoàn tác khi đang đến lượt máy!",
                "Hoàn tác",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Hoàn tác nước đi của AI (nếu có)
        if (vsAI && currentPlayer == PieceColor.WHITE && !board.getMoveHistory().isEmpty()) {
            // Hoàn tác nước đi của AI
            if (board.undoLastMove()) {
                // Hoàn tác nước đi của người chơi
                if (board.undoLastMove()) {
                    currentPlayer = PieceColor.WHITE;
                    updateStatus();
                    clearHighlights();
                    updateBoard();
                    
                    // Xóa 2 dòng cuối trong lịch sử
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
        // Hoàn tác bình thường (không chơi với AI)
        else if (board.undoLastMove()) {
            currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                            PieceColor.BLACK : PieceColor.WHITE;
            updateStatus();
            clearHighlights();
            updateBoard();
            
            // Xóa nước đi cuối trong lịch sử
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
            
            // Kiểm tra xem có cần thay đổi không
            boolean colorChanged = (newAiColor != aiColor);
            boolean difficultyChanged = (aiDifficulty != (Integer) depthCombo.getSelectedItem());
            
            if (colorChanged || difficultyChanged) {
                aiColor = newAiColor;
                
                // Tạo AI mới với cài đặt mới
                chessAI = new ChessAI(aiColor, aiDifficulty);
                
                // Cập nhật trạng thái lượt chơi
                if (colorChanged) {
                    // Đổi lượt hiện tại
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
                
                // Nếu đổi màu AI và hiện tại là lượt của AI, cho AI đi
                if (colorChanged && vsAI && currentPlayer == aiColor) {
                    makeAIMove();
                }
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Đặt Look and Feel theo hệ thống
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            new ChessUI();
        });
    }
}