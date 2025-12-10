package modelAI;

import model.Piece;

public class Move {
    public int fromRow, fromCol, toRow, toCol;
    public Piece moved;
    public Piece captured;
    
    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece moved, Piece captured) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.moved = moved;
        this.captured = captured;
    }
    
    // Thêm phương thức clone để test
    public Move clone() {
        Piece movedClone = moved != null ? moved.clonePiece() : null;
        Piece capturedClone = captured != null ? captured.clonePiece() : null;
        return new Move(fromRow, fromCol, toRow, toCol, movedClone, capturedClone);
    }
}