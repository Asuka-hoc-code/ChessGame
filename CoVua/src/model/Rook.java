package model;
import java.util.ArrayList;
import java.util.List;

import modelAI.Position;

public class Rook extends Piece {

    public Rook(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public char getSymbol() {
        return color == PieceColor.WHITE ? 'R' : 'r';
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        // 1. Đi lên
        for (int r = row - 1; r >= 0; r--) {
            if (board.isEmpty(r, col)) {
                moves.add(new Position(r, col));
            } else {
                if (board.getPiece(r, col).getColor() != this.color) {
                    moves.add(new Position(r, col)); // ăn quân đối phương
                }
                break; // gặp quân → dừng
            }
        }

        // 2. Đi xuống
        for (int r = row + 1; r < 8; r++) {
            if (board.isEmpty(r, col)) {
                moves.add(new Position(r, col));
            } else {
                if (board.getPiece(r, col).getColor() != this.color) {
                    moves.add(new Position(r, col));
                }
                break;
            }
        }

        // 3. Đi trái
        for (int c = col - 1; c >= 0; c--) {
            if (board.isEmpty(row, c)) {
                moves.add(new Position(row, c));
            } else {
                if (board.getPiece(row, c).getColor() != this.color) {
                    moves.add(new Position(row, c));
                }
                break;
            }
        }

        // 4. Đi phải
        for (int c = col + 1; c < 8; c++) {
            if (board.isEmpty(row, c)) {
                moves.add(new Position(row, c));
            } else {
                if (board.getPiece(row, c).getColor() != this.color) {
                    moves.add(new Position(row, c));
                }
                break;
            }
        }

        return moves;
    }

	@Override
	public Piece clonePiece() {
		return new Rook(this.color, this.row, this.col);
	}
}
