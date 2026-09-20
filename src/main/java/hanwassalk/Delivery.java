package hanwassalk;

public class Delivery {
    private final String id;
    private final String area;
    private final int priority;   // 1 = most urgent
    private final double weight;  // kg

    public Delivery(String id, String area, int priority, double weight) {
        this.id = id;
        this.area = area;
        this.priority = priority;
        this.weight = weight;
    }

    public String getId() { return id; }
    public String getArea() { return area; }
    public int getPriority() { return priority; }
    public double getWeight() { return weight; }

    @Override
    public String toString() {
        return "Delivery{id=" + id + ", area=" + area + ", priority=" + priority + ", weight=" + weight + "}";
    }
}