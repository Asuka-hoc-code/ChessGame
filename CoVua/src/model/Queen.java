package model;
import java.util.ArrayList;
import java.util.List;

import modelAI.Position;

public class Queen extends Piece {

    public Queen(PieceColor color, int row, int col) {
        super(color, row, col);
    }

    @Override
    public char getSymbol() {
        return color == PieceColor.WHITE ? 'Q' : 'q';
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        // Kết hợp 8 hướng: Rook + Bishop
        int[] dr = {-1, -1, -1, 0, 1, 1, 1, 0};
        int[] dc = {-1, 0, 1, 1, 1, 0, -1, -1};

        for (int i = 0; i < 8; i++) {
            int r = row + dr[i];
            int c = col + dc[i];

            while (inBounds(r, c)) {
                if (board.isEmpty(r, c)) {
                    moves.add(new Position(r, c));
                } else {
                    if (board.getPiece(r, c).getColor() != this.color) {
                        moves.add(new Position(r, c)); // ăn quân đối phương
                    }
                    break;
                }
                r += dr[i];
                c += dc[i];
            }
        }

        return moves;
    }

	@Override
	public Piece clonePiece() {
		return new Queen(this.color, this.row, this.col);
	}
}
