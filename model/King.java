package model;
import java.util.ArrayList;
import java.util.List;

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

        // Thêm nước đi nhập thành
        // Chỉ kiểm tra nhập thành nếu vua ở vị trí ban đầu
        if (row == (color == PieceColor.WHITE ? 7 : 0) && col == 4) {
            // Nhập thành ngắn (kingside)
            if (board.canCastle(color, true)) {
                moves.add(new Position(row, col + 2));
            }
            // Nhập thành dài (queenside)
            if (board.canCastle(color, false)) {
                moves.add(new Position(row, col - 2));
            }
        }

        return moves;
    }
	@Override
	public Piece clonePiece() {
		return new King(this.color, this.row, this.col);
	}
}
