package hanwassalk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RoutePlanner {

    /**
     * Groups deliveries into trips.
     * 1. Split deliveries by area (case-insensitive).
     * 2. Inside each area, sort by priority (stable sort: ties keep their original order).
     * 3. Put each delivery in the first trip of that area that still has room;
     *    if none has room, open a new trip.
     * 4. Order all trips by their most urgent delivery, then by area name.
     */
    public static List<Trip> plan(List<Delivery> deliveries, double capacity) {
        Map<String, List<Delivery>> byArea = new LinkedHashMap<>();
        for (Delivery d : deliveries) {
            String key = d.getArea().toLowerCase(Locale.ROOT);
            byArea.computeIfAbsent(key, k -> new ArrayList<>()).add(d);
        }

        List<Trip> allTrips = new ArrayList<>();
        for (List<Delivery> group : byArea.values()) {
            group.sort(Comparator.comparingInt(Delivery::getPriority)); // List.sort is stable

            List<Trip> areaTrips = new ArrayList<>();
            for (Delivery d : group) {
                Trip target = null;
                for (Trip trip : areaTrips) {
                    if (trip.canFit(d, capacity)) {
                        target = trip;
                        break;
                    }
                }
                if (target == null) {
                    target = new Trip(d.getArea());
                    areaTrips.add(target);
                }
                target.add(d);
            }
            allTrips.addAll(areaTrips);
        }

        allTrips.sort(Comparator.comparingInt(Trip::getMostUrgentPriority)
                .thenComparing(t -> t.getArea().toLowerCase(Locale.ROOT)));
        return allTrips;
    }
}