package model;

import java.util.ArrayList;
import java.util.List;
import modelAI.Move;
import modelAI.Position;

public class Board {

	private Piece[][] board;
	private List<Move> moveHistory;
	private Position enPassantTarget; // Vị trí tốt có thể bắt qua đường

	public Board() {
		board = new Piece[8][8];
		moveHistory = new ArrayList<>();
		enPassantTarget = null;
		setupDefaultBoard();
	}

	// --- KHỞI TẠO BÀN CỜ MẶC ĐỊNH ---
	private void setupDefaultBoard() {
		// Xóa tất cả quân
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				board[r][c] = null;
			}
		}

		// Pawns
		for (int c = 0; c < 8; c++) {
			board[6][c] = new Pawn(PieceColor.WHITE, 6, c);
			board[1][c] = new Pawn(PieceColor.BLACK, 1, c);
		}

		// Rooks
		board[7][0] = new Rook(PieceColor.WHITE, 7, 0);
		board[7][7] = new Rook(PieceColor.WHITE, 7, 7);
		board[0][0] = new Rook(PieceColor.BLACK, 0, 0);
		board[0][7] = new Rook(PieceColor.BLACK, 0, 7);

		// Knights
		board[7][1] = new Knight(PieceColor.WHITE, 7, 1);
		board[7][6] = new Knight(PieceColor.WHITE, 7, 6);
		board[0][1] = new Knight(PieceColor.BLACK, 0, 1);
		board[0][6] = new Knight(PieceColor.BLACK, 0, 6);

		// Bishops
		board[7][2] = new Bishop(PieceColor.WHITE, 7, 2);
		board[7][5] = new Bishop(PieceColor.WHITE, 7, 5);
		board[0][2] = new Bishop(PieceColor.BLACK, 0, 2);
		board[0][5] = new Bishop(PieceColor.BLACK, 0, 5);

		// Queens
		board[7][3] = new Queen(PieceColor.WHITE, 7, 3);
		board[0][3] = new Queen(PieceColor.BLACK, 0, 3);

		// Kings
		board[7][4] = new King(PieceColor.WHITE, 7, 4);
		board[0][4] = new King(PieceColor.BLACK, 0, 4);
	}

	// --- GETTER / SETTER ---
	public Piece getPiece(int row, int col) {
		if (!inBounds(row, col))
			return null;
		return board[row][col];
	}

	public void setPiece(int row, int col, Piece piece) {
		if (!inBounds(row, col))
			return;
		board[row][col] = piece;
		if (piece != null)
			piece.setPosition(row, col);
	}

	public boolean isEmpty(int row, int col) {
		return inBounds(row, col) && board[row][col] == null;
	}

	public boolean inBounds(int row, int col) {
		return row >= 0 && row < 8 && col >= 0 && col < 8;
	}

	// --- TÌM VUA ---
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

	// --- KIỂM TRA CHIẾU ---
	public boolean isInCheck(PieceColor kingColor) {
		Position kingPos = findKing(kingColor);
		if (kingPos == null)
			return false;

		PieceColor opponentColor = (kingColor == PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;

		// Kiểm tra xem có quân đối phương nào tấn công được vua không
		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				Piece p = board[r][c];
				if (p != null && p.getColor() == opponentColor) {
					List<Position> moves = p.getPossibleMoves(this);
					for (Position move : moves) {
						if (move.row == kingPos.row && move.col == kingPos.col) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	// --- KIỂM TRA NƯỚC ĐI HỢP LỆ ---
	public boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
		// 1. Kiểm tra ô bắt đầu có quân không
		Piece piece = getPiece(fromRow, fromCol);
		if (piece == null) {
			return false; // Không có quân ở ô bắt đầu
		}

		// 2. Kiểm tra quân này có màu của người chơi hiện tại không
		if (piece.getColor() != playerColor) {
			return false; // Không phải quân của bạn
		}

		// 3. Kiểm tra ô đích có quân cùng màu không
		Piece targetPiece = getPiece(toRow, toCol);
		if (targetPiece != null && targetPiece.getColor() == playerColor) {
			return false; // Không thể ăn quân của mình
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
			return false; // Nước đi không hợp lệ theo luật di chuyển
		}

		// 5. Tạo bản sao bàn cờ để thử nghiệm
		Board tempBoard = this.cloneBoard();
		Piece tempPiece = tempBoard.getPiece(fromRow, fromCol);
		Piece capturedPiece = tempBoard.getPiece(toRow, toCol);

		// Thực hiện nước đi trên bản sao
		Move tempMove = new Move(fromRow, fromCol, toRow, toCol, tempPiece, capturedPiece);
		tempBoard.applyMove(tempMove);

		// 6. Kiểm tra sau khi đi, vua có bị chiếu không
		if (tempBoard.isInCheck(playerColor)) {
			return false; // Nước đi này để vua bị chiếu -> không hợp lệ
		}

		return true; // Tất cả điều kiện đều thỏa mãn
	}

	// --- THỰC HIỆN NƯỚC ĐI ---
	public boolean makeMove(int fromRow, int fromCol, int toRow, int toCol, PieceColor playerColor) {
		if (!isValidMove(fromRow, fromCol, toRow, toCol, playerColor)) {
			return false; // Nước đi không hợp lệ
		}

		Piece piece = getPiece(fromRow, fromCol);
		Piece capturedPiece = getPiece(toRow, toCol);

		// Lưu lại nước đi
		Move move = new Move(fromRow, fromCol, toRow, toCol, piece, capturedPiece);
		applyMove(move);
		moveHistory.add(move);

		// Xử lý đặc biệt cho tốt
		if (piece instanceof Pawn) {
			// Phong cấp
			int promotionRow = (piece.getColor() == PieceColor.WHITE) ? 0 : 7;
			if (toRow == promotionRow) {
				board[toRow][toCol] = new Queen(piece.getColor(), toRow, toCol);
			}

			// Bắt tốt qua đường (En Passant)
			if (Math.abs(fromRow - toRow) == 2) {
				// Tốt đi 2 ô, đánh dấu vị trí có thể bắt qua đường
				enPassantTarget = new Position(toRow, toCol);
			} else {
				enPassantTarget = null;
			}
		} else {
			enPassantTarget = null;
		}

		// Xử lý nhập thành (chưa triển khai đầy đủ)
		if (piece instanceof King && Math.abs(fromCol - toCol) == 2) {
			// Đây là nhập thành
			handleCastling(fromRow, fromCol, toRow, toCol);
		}

		return true;
	}

	// --- XỬ LÝ NHẬP THÀNH ---
	private void handleCastling(int fromRow, int fromCol, int toRow, int toCol) {
		// Nhập thành ngắn (kingside)
		if (toCol == 6) {
			// Di chuyển xe từ (fromRow, 7) sang (fromRow, 5)
			Piece rook = getPiece(fromRow, 7);
			if (rook != null && rook instanceof Rook) {
				board[fromRow][5] = rook;
				board[fromRow][7] = null;
				rook.setPosition(fromRow, 5);
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
			}
		}
	}

	// --- UNDO NƯỚC ĐI CUỐI ---
	public boolean undoLastMove() {
		if (moveHistory.isEmpty()) {
			return false;
		}

		Move lastMove = moveHistory.remove(moveHistory.size() - 1);
		undoMove(lastMove);
		return true;
	}

	// --- LẤY TẤT CẢ NƯỚC ĐI HỢP LỆ CỦA 1 BÊN ---
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

	// --- KIỂM TRA CHIẾU HẾT ---
	public boolean isCheckmate(PieceColor side) {
		// 1. Vua đang bị chiếu
		// 2. Không có nước đi hợp lệ nào
		return isInCheck(side) && getLegalMoves(side).isEmpty();
	}

	// --- KIỂM TRA HẾT ĐƯỜNG ĐI (STALEMATE) ---
	public boolean isStalemate(PieceColor side) {
		// 1. Vua KHÔNG bị chiếu
		// 2. Không có nước đi hợp lệ nào
		return !isInCheck(side) && getLegalMoves(side).isEmpty();
	}

	// --- KIỂM TRA CỜ HÒA ---
	public boolean isDraw() {
		// Có thể thêm các điều kiện hòa khác:
		// 1. Hết nước đi (stalemate)
		// 2. Lặp lại nước đi 3 lần
		// 3. 50 nước không ăn quân
		// 4. Không đủ lực lượng chiếu hết

		return isStalemate(PieceColor.WHITE) || isStalemate(PieceColor.BLACK);
	}

	// --- APPLY / UNDO MOVE ---
	public void applyMove(Move m) {
		setPiece(m.toRow, m.toCol, m.moved);
		setPiece(m.fromRow, m.fromCol, null);
	}

	public void undoMove(Move m) {
		setPiece(m.fromRow, m.fromCol, m.moved);
		setPiece(m.toRow, m.toCol, m.captured);
	}

	// --- CLONE BÀN CỜ ---
	public Board cloneBoard() {
		Board copy = new Board();
		copy.board = new Piece[8][8];

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				Piece p = board[r][c];
				if (p != null)
					copy.board[r][c] = p.clonePiece();
			}
		}

		copy.enPassantTarget = (enPassantTarget != null) ? new Position(enPassantTarget.row, enPassantTarget.col)
				: null;

		return copy;
	}

	// --- IN BÀN CỜ ---
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
	}

	// --- GETTERS ---
	public List<Move> getMoveHistory() {
		return new ArrayList<>(moveHistory);
	}

	public Position getEnPassantTarget() {
		return enPassantTarget;
	}
}