package hanwassalk;

import java.util.ArrayList;
import java.util.List;

public class Trip {
    // protects against floating-point errors such as 8.8 + 1.2 = 10.000000000000002
    static final double EPSILON = 1e-9;

    private final String area;
    private final List<Delivery> deliveries = new ArrayList<>();
    private double load = 0;
    private int mostUrgentPriority = Integer.MAX_VALUE;

    public Trip(String area) {
        this.area = area;
    }

    public boolean canFit(Delivery delivery, double capacity) {
        return load + delivery.getWeight() <= capacity + EPSILON;
    }

    public void add(Delivery delivery) {
        deliveries.add(delivery);
        load += delivery.getWeight();
        mostUrgentPriority = Math.min(mostUrgentPriority, delivery.getPriority());
    }

    public String getArea() { return area; }
    public List<Delivery> getDeliveries() { return deliveries; }
    public double getLoad() { return load; }
    public int getMostUrgentPriority() { return mostUrgentPriority; }
}