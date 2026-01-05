package model;

public class Move {
    public int fromRow, fromCol, toRow, toCol;
    public Piece moved;
    public Piece captured;
    public boolean movedHadMoved;   
    public boolean capturedHadMoved; 
    
    public Move(int fromRow, int fromCol, int toRow, int toCol, Piece moved, Piece captured) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.moved = moved;
        this.captured = captured;
        this.movedHadMoved = moved != null ? moved.hasMoved() : false;
        this.capturedHadMoved = captured != null ? captured.hasMoved() : false;
    }
    
    @Override
    public Move clone() {
        Move clone = new Move(fromRow, fromCol, toRow, toCol, 
                               moved != null ? moved.clonePiece() : null, 
                               captured != null ? captured.clonePiece() : null);
        clone.movedHadMoved = this.movedHadMoved;
        clone.capturedHadMoved = this.capturedHadMoved;
        return clone;
    }
    
    @Override
    public String toString() {
        return String.format("Move[%d,%d -> %d,%d]", fromRow, fromCol, toRow, toCol);
    }
}