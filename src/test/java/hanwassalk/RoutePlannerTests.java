package hanwassalk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple tests with no external libraries.
 * Run: java -cp out hanwassalk.RoutePlannerTests
 * (Later, in a bigger project, these can be moved to JUnit.)
 */
public class RoutePlannerTests {
    private static int failures = 0;

    public static void main(String[] args) throws IOException {
        noDeliveries();
        overweightPackageIsSkippedWithReason();
        exactlyTenKgIsAllowed();
        nextPackageOverCapacityStartsNewTrip();
        samePriorityKeepsFileOrder();
        urgentTripsComeFirst();
        floatingPointSumDoesNotBreakExactCapacity();
        everyDeliveryInExactlyOneTripAndNoneOverloaded();
        exportWritesHeaderAndOneLinePerDelivery();
        exportOfNoTripsWritesOnlyHeader();

        if (failures == 0) {
            System.out.println("All tests passed.");
        } else {
            System.out.println(failures + " test(s) FAILED.");
            System.exit(1);
        }
    }

    private static void check(String name, boolean condition) {
        System.out.println((condition ? "  ok    " : "  FAIL  ") + name);
        if (!condition) failures++;
    }

    private static ReadResult readCsv(String text) throws IOException {
        Path file = Files.createTempFile("deliveries", ".csv");
        try {
            Files.write(file, text.getBytes("UTF-8"));
            return DeliveryReader.read(file, 10.0);
        } finally {
            Files.delete(file);
        }
    }

    private static void noDeliveries() throws IOException {
        ReadResult r = readCsv("id,area,priority,weight\n");
        check("no deliveries -> no trips, nothing skipped",
                RoutePlanner.plan(r.getValid(), 10).isEmpty() && r.getSkipped().isEmpty());
    }

    private static void overweightPackageIsSkippedWithReason() throws IOException {
        ReadResult r = readCsv("id,area,priority,weight\n1,A,1,10.5\n2,A,1,3\n");
        check("overweight package is skipped with a reason",
                r.getValid().size() == 1 && r.getValid().get(0).getId().equals("2")
                        && r.getSkipped().get(0).getReason().contains("heavier"));
    }

    private static void exactlyTenKgIsAllowed() {
        List<Trip> trips = RoutePlanner.plan(
                Arrays.asList(new Delivery("1", "A", 1, 4.5), new Delivery("2", "A", 1, 5.5)), 10);
        check("exactly 10 kg fits in one trip", trips.size() == 1);
    }

    private static void nextPackageOverCapacityStartsNewTrip() {
        List<Trip> trips = RoutePlanner.plan(
                Arrays.asList(new Delivery("1", "A", 1, 6), new Delivery("2", "A", 1, 5)), 10);
        check("package that would exceed capacity starts a new trip", trips.size() == 2);
    }

    private static void samePriorityKeepsFileOrder() {
        List<Trip> trips = RoutePlanner.plan(
                Arrays.asList(new Delivery("b", "A", 1, 1), new Delivery("a", "A", 1, 1)), 10);
        check("same priority keeps file order",
                trips.get(0).getDeliveries().get(0).getId().equals("b"));
    }

    private static void urgentTripsComeFirst() {
        List<Trip> trips = RoutePlanner.plan(
                Arrays.asList(new Delivery("1", "A", 3, 1), new Delivery("2", "B", 1, 1)), 10);
        check("more urgent trip is listed first", trips.get(0).getArea().equals("B"));
    }

    private static void floatingPointSumDoesNotBreakExactCapacity() {
        List<Trip> trips = RoutePlanner.plan(Arrays.asList(
                new Delivery("1", "A", 1, 4.5), new Delivery("2", "A", 1, 4.3),
                new Delivery("3", "A", 2, 1.2)), 10);
        check("4.5 + 4.3 + 1.2 fits in one 10 kg trip", trips.size() == 1);
    }

    private static void everyDeliveryInExactlyOneTripAndNoneOverloaded() {
        List<Delivery> deliveries = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            deliveries.add(new Delivery(String.valueOf(i), i % 2 == 0 ? "A" : "B", i % 3 + 1, 1.5 * (i % 5) + 0.5));
        }
        List<Trip> trips = RoutePlanner.plan(deliveries, 10);
        int placed = 0;
        boolean noneOverloaded = true;
        for (Trip t : trips) {
            placed += t.getDeliveries().size();
            if (t.getLoad() > 10 + 1e-9) noneOverloaded = false;
        }
        check("every delivery is in exactly one trip, none overloaded", placed == 40 && noneOverloaded);
    }

    private static void exportWritesHeaderAndOneLinePerDelivery() throws IOException {
        List<Trip> trips = RoutePlanner.plan(Arrays.asList(
                new Delivery("1", "Maadi", 1, 2.0), new Delivery("2", "Maadi", 2, 3.5),
                new Delivery("3", "Zamalek", 1, 7.0)), 10);
        Path file = Files.createTempFile("plan", ".csv");
        try {
            PlanWriter.write(file, trips);
            List<String> lines = Files.readAllLines(file);
            check("export: header + one line per delivery",
                    lines.size() == 4 && lines.get(0).equals(PlanWriter.HEADER));
            check("export: line content is correct", lines.get(1).equals("1,Maadi,1,1,2,5.5"));
        } finally {
            Files.delete(file);
        }
    }

    private static void exportOfNoTripsWritesOnlyHeader() throws IOException {
        Path file = Files.createTempFile("plan", ".csv");
        try {
            PlanWriter.write(file, new ArrayList<Trip>());
            check("export: no trips -> only the header", Files.readAllLines(file).size() == 1);
        } finally {
            Files.delete(file);
        }
    }
}