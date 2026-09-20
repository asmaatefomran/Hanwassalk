package hanwassalk;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

public class Main {
    static final double DEFAULT_CAPACITY_KG = 10.0;
    static final String USAGE =
            "Usage: java -cp out hanwassalk.Main <input.csv> [--capacity <kg>] [--output <plan.csv>]";

    public static void main(String[] args) {
        String inputFile = null;
        String outputFile = null;
        double capacity = DEFAULT_CAPACITY_KG;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--capacity")) {
                if (i + 1 >= args.length) fail("--capacity needs a value.");
                try {
                    capacity = Double.parseDouble(args[++i]);
                } catch (NumberFormatException e) {
                    fail("capacity must be a number.");
                }
                if (!(capacity > 0)) fail("capacity must be greater than 0.");
            } else if (arg.equals("--output")) {
                if (i + 1 >= args.length) fail("--output needs a file name.");
                outputFile = args[++i];
            } else if (inputFile == null) {
                inputFile = arg;
            } else {
                fail("unknown argument: " + arg);
            }
        }
        if (inputFile == null) {
            System.err.println(USAGE);
            System.exit(1);
        }

        try {
            ReadResult result = DeliveryReader.read(Paths.get(inputFile), capacity);
            List<Trip> trips = RoutePlanner.plan(result.getValid(), capacity);
            printReport(trips, result.getSkipped(), capacity);

            if (outputFile != null) { // extension: export the plan
                PlanWriter.write(Paths.get(outputFile), trips);
                System.out.println("Plan exported to: " + outputFile);
            }
        } catch (NoSuchFileException e) {
            System.err.println("Error: file not found: " + e.getFile());
            System.exit(1);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void fail(String message) {
        System.err.println("Error: " + message);
        System.err.println(USAGE);
        System.exit(1);
    }

    static void printReport(List<Trip> trips, List<SkippedRow> skipped, double capacity) {
        if (trips.isEmpty()) {
            System.out.println("No deliveries to plan.");
        }

        int planned = 0;
        int tripNumber = 1;
        for (Trip trip : trips) {
            System.out.println("Trip " + tripNumber++ + " - " + trip.getArea()
                    + " (most urgent priority: " + trip.getMostUrgentPriority() + ")");
            for (Delivery d : trip.getDeliveries()) {
                System.out.println("    #" + d.getId() + "  priority " + d.getPriority()
                        + "  " + Format.number(d.getWeight()) + " kg");
                planned++;
            }
            double remaining = Math.max(capacity - trip.getLoad(), 0);
            System.out.println("    Load: " + Format.number(trip.getLoad()) + " / " + Format.number(capacity)
                    + " kg  (remaining: " + String.format(Locale.ROOT, "%.2f", remaining) + " kg)");
            System.out.println();
        }

        // Rows that could not be planned are reported, never silently dropped.
        if (!skipped.isEmpty()) {
            System.out.println("Skipped rows (" + skipped.size() + "):");
            for (SkippedRow s : skipped) {
                System.out.println("    row " + s.getRowNumber() + ": " + s.getRawText() + "  ->  " + s.getReason());
            }
            System.out.println();
        }

        System.out.println("Summary: " + planned + " deliveries planned in " + trips.size()
                + " trip(s), " + skipped.size() + " skipped.");
    }
}