package hanwassalk;

/** A CSV row that could not be planned, and why. */
public class SkippedRow {
    private final int rowNumber;
    private final String rawText;
    private final String reason;

    public SkippedRow(int rowNumber, String rawText, String reason) {
        this.rowNumber = rowNumber;
        this.rawText = rawText;
        this.reason = reason;
    }

    public int getRowNumber() { return rowNumber; }
    public String getRawText() { return rawText; }
    public String getReason() { return reason; }
}