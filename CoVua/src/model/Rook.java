package model;
import java.util.ArrayList;
import java.util.List;

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

        for (int r = row - 1; r >= 0; r--) {
            if (board.isEmpty(r, col)) {
                moves.add(new Position(r, col));
            } else {
                if (board.getPiece(r, col).getColor() != this.color) {
                    moves.add(new Position(r, col)); 
                }
                break; 
            }
        }

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
