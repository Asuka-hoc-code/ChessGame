package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import model.*;

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
    
    public ChessUI() {
        board = new Board();
        currentPlayer = PieceColor.WHITE;
        selectedPosition = null;
        
        initializeUI();
        updateBoard();
    }
    
    private void initializeUI() {
        setTitle("Java Chess Game - Cờ Vua Hoàn Chỉnh");
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
        statusLabel = new JLabel("Lượt: TRẮNG");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Nút chức năng
        JButton newGameBtn = createButton("Trận mới", e -> newGame());
        JButton undoBtn = createButton("Hoàn tác", e -> undoMove());
        JButton resignBtn = createButton("Đầu hàng", e -> resign());
        JButton helpBtn = createButton("Nước đi hợp lệ", e -> showLegalMoves());
        
        // Khu vực lịch sử nước đi
        JLabel historyLabel = new JLabel("Lịch sử nước đi:");
        historyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        moveHistoryArea = new JTextArea(10, 15);
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        
        // Thêm các thành phần vào infoPanel
        infoPanel.add(statusLabel);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(newGameBtn);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(undoBtn);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(resignBtn);
        infoPanel.add(Box.createVerticalStrut(5));
        infoPanel.add(helpBtn);
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
        JMenuItem exitItem = new JMenuItem("Thoát");
        
        newGameItem.addActionListener(e -> newGame());
        undoItem.addActionListener(e -> undoMove());
        resignItem.addActionListener(e -> resign());
        exitItem.addActionListener(e -> System.exit(0));
        
        gameMenu.add(newGameItem);
        gameMenu.add(undoItem);
        gameMenu.add(resignItem);
        gameMenu.addSeparator();
        gameMenu.add(exitItem);
        
        JMenu helpMenu = new JMenu("Trợ giúp");
        JMenuItem movesItem = new JMenuItem("Hiển thị nước đi hợp lệ");
        JMenuItem rulesItem = new JMenuItem("Luật chơi");
        JMenuItem aboutItem = new JMenuItem("Giới thiệu");
        
        movesItem.addActionListener(e -> showLegalMoves());
        rulesItem.addActionListener(e -> showRules());
        aboutItem.addActionListener(e -> showAbout());
        
        helpMenu.add(movesItem);
        helpMenu.add(rulesItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        menuBar.add(gameMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void handleSquareClick(int row, int col) {
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
                
                // Thêm vào lịch sử
                addMoveToHistory(fromRow, fromCol, row, col, isCastlingMove);
                
                // Kiểm tra kết thúc trò chơi
                checkGameEnd();
                
                if (!board.isCheckmate(currentPlayer) && !board.isDraw()) {
                    // Đổi lượt
                    currentPlayer = (currentPlayer == PieceColor.WHITE) ? 
                                    PieceColor.BLACK : PieceColor.WHITE;
                    updateStatus();
                }
                
                // Xóa selection và highlight
                clearHighlights();
                selectedPosition = null;
                updateBoard();
            } else {
                // Nước đi không hợp lệ
                JOptionPane.showMessageDialog(this, 
                    "Nước đi không hợp lệ!", 
                    "Lỗi", 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    // Xử lý phong cấp nếu cần
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
    
    // Hiển thị dialog chọn quân phong cấp - ĐÃ SỬA
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
    
    // Áp dụng quân phong cấp đã chọn
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
            moveHistoryArea.append(" → Phong cấp thành " + pieceName + "\n");
        }
    }
    
    private void addMoveToHistory(int fromRow, int fromCol, int toRow, int toCol, boolean isCastling) {
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
        
        moveHistoryArea.append(moveNotation + "\n");
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
    
    // ĐÃ THÊM SwingUtilities.invokeLater() cho thread safety
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
                statusLabel.setText("Lượt: TRẮNG (ĐANG BỊ CHIẾU!)");
                statusLabel.setForeground(Color.RED);
            } else if (board.isInCheck(PieceColor.BLACK)) {
                statusLabel.setText("Lượt: ĐEN (ĐANG BỊ CHIẾU!)");
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
        if (currentPlayer == PieceColor.WHITE) {
            statusLabel.setText("Lượt: TRẮNG");
        } else {
            statusLabel.setText("Lượt: ĐEN");
        }
    }
    
    private void checkGameEnd() {
        if (board.isCheckmate(PieceColor.WHITE)) {
            JOptionPane.showMessageDialog(this,
                "CHIẾU HẾT!\nĐEN THẮNG!",
                "Trận đấu kết thúc",
                JOptionPane.INFORMATION_MESSAGE);
            disableBoard();
        } else if (board.isCheckmate(PieceColor.BLACK)) {
            JOptionPane.showMessageDialog(this,
                "CHIẾU HẾT!\nTRẮNG THẮNG!",
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
        if (board.undoLastMove()) {
            // Đổi lượt về người chơi trước
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
                for (int i = 0; i < lines.length - 1; i++) {
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
        String winner = (currentPlayer == PieceColor.WHITE) ? "ĐEN" : "TRẮNG";
        String resigner = (currentPlayer == PieceColor.WHITE) ? "TRẮNG" : "ĐEN";
        
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
    
    private void showLegalMoves() {
        List<Move> legalMoves = board.getLegalMoves(currentPlayer);
        
        if (legalMoves.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Không có nước đi hợp lệ nào!",
                "Nước đi hợp lệ",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Các nước đi hợp lệ cho ").append(currentPlayer == PieceColor.WHITE ? "TRẮNG" : "ĐEN").append(":\n\n");
        
        for (Move move : legalMoves) {
            char fromColChar = (char)('a' + move.fromCol);
            char toColChar = (char)('a' + move.toCol);
            
            // Kiểm tra nếu là nước đi nhập thành
            if (move.moved instanceof King && Math.abs(move.fromCol - move.toCol) == 2) {
                boolean kingside = (move.toCol > move.fromCol);
                sb.append(kingside ? "  O-O (Nhập thành ngắn)\n" : "  O-O-O (Nhập thành dài)\n");
            } else {
                String pieceName = getPieceName(move.moved.getSymbol());
                sb.append(String.format("  %s %c%d → %c%d%n",
                    pieceName,
                    fromColChar, (8 - move.fromRow),
                    toColChar, (8 - move.toRow)));
            }
        }
        
        sb.append("\nTổng cộng: ").append(legalMoves.size()).append(" nước đi");
        
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(300, 400));
        
        JOptionPane.showMessageDialog(this,
            scrollPane,
            "Nước đi hợp lệ",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String getPieceName(char symbol) {
        switch (Character.toUpperCase(symbol)) {
            case 'K': return "Vua";
            case 'Q': return "Hậu";
            case 'R': return "Xe";
            case 'B': return "Tượng";
            case 'N': return "Mã";
            case 'P': return "Tốt";
            default: return "?";
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