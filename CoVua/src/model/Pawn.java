package model;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece{

	public Pawn(PieceColor color, int row, int col) {
		super(color, row, col);
	}

	@Override
	 public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        int direction = (color == PieceColor.WHITE) ? -1 : 1; // trắng đi lên, đen đi xuống

        int nextRow = row + direction;

        // 1. Đi thẳng 1 ô
        if (inBounds(nextRow, col) && board.isEmpty(nextRow, col)) {
            moves.add(new Position(nextRow, col));

            // 2. Đi thẳng 2 ô nếu chưa đi
            int startRow = (color == PieceColor.WHITE) ? 6 : 1;
            int nextRow2 = row + 2 * direction;
            if (row == startRow && board.isEmpty(nextRow2, col)) {
                moves.add(new Position(nextRow2, col));
            }
        }

        // 3. Ăn chéo
        int[] dc = {-1, 1};
        for (int d : dc) {
            int newCol = col + d;
            if (inBounds(nextRow, newCol)) {
                Piece target = board.getPiece(nextRow, newCol);
                if (target != null && target.getColor() != this.color) {
                    moves.add(new Position(nextRow, newCol));
                }
            }
        }

        return moves;
    }


	@Override
	public char getSymbol() {
		return color == PieceColor.WHITE ? 'P' : 'p';
	}

	@Override
	public Piece clonePiece() {
		return new Pawn(this.color, this.row, this.col);
	}
	

}
