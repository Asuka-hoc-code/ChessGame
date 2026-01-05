package modelAI;

public class MainTest {
    public static void main(String[] args) {
        modelAI.ChessAI ai = new modelAI.ChessAI(model.PieceColor.WHITE, 6);
        ai.runBenchmark();
    }
}