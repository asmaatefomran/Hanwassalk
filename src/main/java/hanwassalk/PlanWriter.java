package hanwassalk;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * EXTENSION: exports the planned trips to a CSV file (one line per delivery),
 * so the plan can be opened in Excel, shared with drivers, or read by another program.
 * Only planned deliveries are written; skipped rows appear in the console report.
 * An existing file with the same name is overwritten.
 */
public class PlanWriter {
    public static final String HEADER = "trip,area,delivery_id,priority,weight_kg,trip_load_kg";

    public static void write(Path file, List<Trip> trips) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            int tripNumber = 1;
            for (Trip trip : trips) {
                for (Delivery d : trip.getDeliveries()) {
                    writer.write(tripNumber + "," + trip.getArea() + "," + d.getId() + ","
                            + d.getPriority() + "," + Format.number(d.getWeight()) + ","
                            + Format.number(trip.getLoad()));
                    writer.newLine();
                }
                tripNumber++;
            }
        }
    }
}