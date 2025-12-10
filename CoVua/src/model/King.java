package model;
import java.util.ArrayList;
import java.util.List;

import modelAI.Position;

public class King extends Piece {

    public King(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public char getSymbol() {
        return color == PieceColor.WHITE ? 'K' : 'k';
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int[] dr = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dc = {-1, 0, 1, 1, 1, 0, -1, -1};

        for (int i = 0; i < 8; i++) {
            int r = row + dr[i];
            int c = col + dc[i];
            if (inBounds(r, c)) {
                Piece target = board.getPiece(r, c);
                if (target == null || target.getColor() != this.color) {
                    moves.add(new Position(r, c));
                }
            }
        }

        // Castling có thể xử lý thêm ở Game nếu muốn
        return moves;
    }

	@Override
	public Piece clonePiece() {
		return new King(this.color, this.row, this.col);
	}
}
