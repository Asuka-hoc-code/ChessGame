package modelAI;

public class Position {
    public int row;
    public int col;
    
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position) {
            Position other = (Position) obj;
            return this.row == other.row && this.col == other.col;
        }
        return false;
    }
    
    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}