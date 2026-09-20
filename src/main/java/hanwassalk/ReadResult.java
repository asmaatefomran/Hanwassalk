package hanwassalk;

import java.util.ArrayList;
import java.util.List;

/** What the reader returns: the good deliveries and the rows it had to skip. */
public class ReadResult {
    private final List<Delivery> valid = new ArrayList<>();
    private final List<SkippedRow> skipped = new ArrayList<>();

    public List<Delivery> getValid() { return valid; }
    public List<SkippedRow> getSkipped() { return skipped; }
}