package battleship;

public class ScoreEntry {
    private String date;
    private int moves;

    public ScoreEntry(String date, int moves) {
        this.date = date;
        this.moves = moves;
    }

    public String getDate() { return date; }
    public int getMoves() { return moves; }

    @Override
    public String toString() {
        return date + " - " + moves + " jogadas";
    }
}
