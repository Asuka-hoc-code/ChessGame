package model;
import java.util.List;

import modelAI.Position;

public abstract class Piece {
    protected PieceColor color;
    protected int row;
    protected int col;

    public Piece(PieceColor color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
    }

    public PieceColor getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // Mỗi quân cờ phải tự định nghĩa nước đi
    public abstract List<Position> getPossibleMoves(Board board);

    // Lấy ký tự hiển thị ('K','Q','R','B','N','P')
    public abstract char getSymbol();

    // Helper: kiểm tra ô hợp lệ
    protected boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }
    public abstract Piece clonePiece();
}
