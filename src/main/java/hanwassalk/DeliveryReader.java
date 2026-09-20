package hanwassalk;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Reads deliveries from a CSV file with the columns: id,area,priority,weight
 * (any order; header names are case-insensitive).
 * Note: this simple reader does not support commas inside values.
 */
public class DeliveryReader {
    private static final String NUMBER = "\\d+(\\.\\d+)?|\\.\\d+";

    public static ReadResult read(Path file, double capacity) throws IOException {
        ReadResult result = new ReadResult();
        Set<String> seenIds = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return result; // completely empty file
            }
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1); // remove the BOM that Excel may add
            }

            String[] header = headerLine.split(",", -1);
            for (int i = 0; i < header.length; i++) {
                header[i] = header[i].trim().toLowerCase(Locale.ROOT);
            }
            int idCol = columnOf(header, "id");
            int areaCol = columnOf(header, "area");
            int priorityCol = columnOf(header, "priority");
            int weightCol = columnOf(header, "weight");

            String line;
            int rowNumber = 1; // the header was row 1
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.trim().isEmpty()) {
                    continue; // ignore blank lines
                }
                parseRow(line, rowNumber, header.length, idCol, areaCol, priorityCol, weightCol,
                        capacity, seenIds, result);
            }
        }
        return result;
    }

    private static int columnOf(String[] header, String name) {
        for (int i = 0; i < header.length; i++) {
            if (header[i].equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException("Input file is missing column: " + name);
    }

    private static void parseRow(String line, int rowNumber, int columnCount,
                                 int idCol, int areaCol, int priorityCol, int weightCol,
                                 double capacity, Set<String> seenIds, ReadResult result) {
        String[] parts = line.split(",", -1);
        if (parts.length != columnCount) {
            skip(result, rowNumber, line, "expected " + columnCount + " columns but found " + parts.length);
            return;
        }

        String id = parts[idCol].trim();
        String area = String.join(" ", parts[areaCol].trim().split("\\s+")); // collapse extra spaces
        String priorityText = parts[priorityCol].trim();
        String weightText = parts[weightCol].trim();

        if (id.isEmpty()) {
            skip(result, rowNumber, line, "missing id");
            return;
        }
        if (area.isEmpty()) {
            skip(result, rowNumber, line, "missing area");
            return;
        }

        int priority;
        try {
            priority = Integer.parseInt(priorityText);
        } catch (NumberFormatException e) {
            skip(result, rowNumber, line, "priority must be a whole number >= 1");
            return;
        }
        if (priority < 1) {
            skip(result, rowNumber, line, "priority must be a whole number >= 1");
            return;
        }

        if (!weightText.matches(NUMBER)) {
            skip(result, rowNumber, line, "weight must be a number > 0");
            return;
        }
        double weight = Double.parseDouble(weightText);
        if (weight <= 0) {
            skip(result, rowNumber, line, "weight must be a number > 0");
            return;
        }

        if (weight > capacity + Trip.EPSILON) {
            skip(result, rowNumber, line, "package (" + Format.number(weight)
                    + " kg) is heavier than vehicle capacity (" + Format.number(capacity) + " kg)");
            return;
        }

        if (!seenIds.add(id)) {
            skip(result, rowNumber, line, "duplicate id '" + id + "'");
            return;
        }

        result.getValid().add(new Delivery(id, area, priority, weight));
    }

    private static void skip(ReadResult result, int rowNumber, String line, String reason) {
        result.getSkipped().add(new SkippedRow(rowNumber, line, reason));
    }
}