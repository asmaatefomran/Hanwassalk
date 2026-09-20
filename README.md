# Hanwassalk - Delivery Route Planner (Java)

A small Java program that reads delivery requests from a CSV file and groups them into trips of at most 10 kg each.

## How to run

Requires **JDK 11 or newer** (a JDK, not only a JRE, so that `javac` is available). No external libraries and no build tool are needed.

### Option 1: command line

Open a terminal in the project folder (the one that contains `src` and `data`) and run the steps **in this order**:

```bash
# 1. Compile the program (creates the "out" folder)
javac -d out -sourcepath src/main/java src/main/java/hanwassalk/Main.java

# 2. Run it
java -cp out hanwassalk.Main data/sample_deliveries.csv
```

More examples:

```bash
java -cp out hanwassalk.Main data/edge_cases.csv                        # bad rows, ties, exact-capacity cases
```
![img.png](img.png)

```bash
java -cp out hanwassalk.Main data/empty.csv                             # no deliveries
```
![img_1.png](img_1.png)
```bash
java -cp out hanwassalk.Main data/sample_deliveries.csv --capacity 8    # change the vehicle capacity (kg)
```
![img_2.png](img_2.png)
```bash
java -cp out hanwassalk.Main data/sample_deliveries.csv --output plan.csv   # extension: export the plan
```
![img_3.png](img_3.png)
![img_4.png](img_4.png)

Run the tests (the program must be compiled first, see step 1):

```bash
javac -d out -cp out src/test/java/hanwassalk/RoutePlannerTests.java
java -cp out hanwassalk.RoutePlannerTests
```
![img_5.png](img_5.png)



### Option 2: IntelliJ IDEA (Run icon)

1. Open the project folder. Right-click `src/main/java` -> *Mark Directory as* -> *Sources Root*, and `src/test/java` -> *Test Sources Root*.
2. Open `Main.java` and click the green run icon next to `main`. It will print the usage message once because no input file was given; this creates the run configuration.
3. Open the configuration dropdown -> *Edit Configurations...* and set:
    - **Program arguments:** `data/sample_deliveries.csv` (optionally add `--output plan.csv`)
    - **Working directory:** the project folder (the one containing `data` and `src`)
4. Click the green run icon again.

To run the tests, open `RoutePlannerTests.java` and click the run icon next to its `main` method (no arguments needed).

### Expected output for `data/sample_deliveries.csv`

```
Trip 1 - Maadi (most urgent priority: 1)
    #2  priority 1  2 kg
    #5  priority 2  3.5 kg
    Load: 5.5 / 10 kg  (remaining: 4.50 kg)

Trip 2 - Zamalek (most urgent priority: 1)
    #4  priority 1  7 kg
    Load: 7 / 10 kg  (remaining: 3.00 kg)

Trip 3 - Nasr City (most urgent priority: 2)
    #1  priority 2  4.5 kg
    #3  priority 3  1.2 kg
    Load: 5.7 / 10 kg  (remaining: 4.30 kg)

Summary: 5 deliveries planned in 3 trip(s), 0 skipped.
```

### Where is the exported plan?

`plan.csv` is created only when `--output` is used, and a relative name is saved in the **working directory** (the folder you ran the command from; the project folder in IntelliJ). The program prints `Plan exported to: ...` when it succeeds. You can also give a full path, for example `--output C:\Users\me\Desktop\plan.csv`.

### Troubleshooting

| Problem | Cause and fix |
|---|---|
| `illegal character: '\ufeff'` when compiling | A file was saved as "UTF-8 with BOM" (some Windows editors do this). In IntelliJ: right-click the file tab -> *Remove BOM*. Set *Settings -> Editor -> File Encodings -> Create UTF-8 files* to *with NO BOM*. |
| `cannot find symbol` when compiling the tests | The program was not compiled first. Run step 1 again. |
| `Could not find or load main class` | Wrong class name or wrong folder. Use `hanwassalk.Main` / `hanwassalk.RoutePlannerTests` and run from the project folder. |
| `file not found: data/...` | The working directory is wrong. It must be the folder that contains `data` and `src`. |
| `javac` is not recognized | Only a JRE is installed, or the JDK is not on the PATH. Install a JDK (for example Temurin 17). |

## Project structure

| File | Responsibility |
|---|---|
| `Delivery.java` | One delivery request (id, area, priority, weight) |
| `Trip.java` | One trip: its deliveries, current load, most urgent priority |
| `DeliveryReader.java` | Reads and validates the CSV file |
| `RoutePlanner.java` | The grouping algorithm |
| `Main.java` | Command-line entry point and report printing |
| `PlanWriter.java` | Extension: exports the plan to a CSV file |
| `SkippedRow.java`, `ReadResult.java`, `Format.java` | Small helpers |
| `RoutePlannerTests.java` | Tests (plain Java, no libraries) |

## Input format

CSV with a header row and these columns (any order, case-insensitive):

```
id,area,priority,weight
1,Nasr City,2,4.5
2,Maadi,1,2.0
```

- `id`: text, must be unique
- `area`: text (`Nasr City` and `nasr city` are treated as the same area). Commas inside values are not supported.
- `priority`: whole number >= 1 (1 = most urgent)
- `weight`: kg, number > 0

## Output

Trips are listed from most urgent to least urgent. Each trip shows its deliveries, total load and remaining capacity. Rows that could not be planned are listed at the end with the row number and the reason, so nothing is silently dropped.

With `--output plan.csv` the plan is also saved to a file (see the extension below).

## How I handled the edge cases

| Case | Decision |
|---|---|
| No deliveries | Prints "No deliveries to plan." and exits normally. |
| Package heavier than 10 kg | It can never fit in any trip, so it is **skipped and reported** with a reason (not silently dropped, not split). |
| Same priority | Sorted with a stable sort, so ties keep their order from the file. The result is always the same for the same input. |
| Next package would exceed capacity | It is not added to that trip. It goes into another trip of the same area that has room, or starts a new trip. A total of exactly 10 kg is allowed. |
| Invalid rows (missing values, non-numbers, weight <= 0, duplicate id, wrong number of columns) | Skipped and reported, so one bad row does not stop the whole run. |

## Extension: export the plan to a CSV file (`--output`)

Add `--output plan.csv` and the program saves the plan to a file, one line per delivery:

```
trip,area,delivery_id,priority,weight_kg,trip_load_kg
1,Maadi,2,1,2,5.5
1,Maadi,5,2,3.5,5.5
2,Zamalek,4,1,7,7
```

**Why I chose it:** printing to the screen is fine for reading, but a dispatcher cannot easily use it. A CSV file opens directly in Excel or Google Sheets, can be shared with drivers, and can be read by another program. It is also a natural first step if this becomes a bigger project (for example, feeding the plan into another system).

**Why it is small on purpose:** it is one short class (`PlanWriter`) with no libraries and one extra command-line flag. Only planned deliveries are written; skipped rows are shown in the console report. An existing file with the same name is overwritten.

## Reasoning questions

**1. Explain your solution approach in your own words.**

I split the deliveries by area first. Inside each area I sort by priority so the urgent ones come first. Then I go through them one by one and put each into the first trip of that area that still has room. If none has room, I open a new trip. At the end I sort all trips so the trips containing the most urgent deliveries are shown first. Before all this, `DeliveryReader` checks every row and sets aside anything that is invalid or too heavy.

**2. What was the most difficult part of the assignment?**

Deciding what to do when two rules pull in different directions: keeping an area together and handling urgent deliveries first. I chose to keep trips single-area and to order trips by their most urgent delivery. Another tricky part was floating-point numbers: 8.8 + 1.2 can come out as 10.000000000000002 with `double`, which would wrongly reject an exact 10 kg trip, so I compare with a tiny tolerance (`EPSILON`).

**3. Are there situations where your algorithm may not produce the best possible grouping? Explain.**

Yes. It is a greedy method, so it does not always use the fewest trips. Example: one area with four packages of 4 kg, 4 kg, 6 kg and 6 kg, in that priority order. My algorithm puts the two 4 kg packages together (8 kg), then each 6 kg package needs its own trip, giving 3 trips. Pairing each 4 kg with a 6 kg package would need only 2 trips. This is the bin packing problem, which is hard to solve perfectly, and the priority order forces the algorithm to place packages in a fixed order.

Also, trips never mix areas, so an area with a single small package gets its own mostly empty trip, even if another area's trip has room. Finally, urgency only decides the order inside an area, so a very urgent package can end up in a later trip than a less urgent one if they are in different areas.

**4. If the input contained 1,000,000 delivery requests, what part of your solution might become slow or memory-intensive?**

The first-fit search. For each delivery I loop over the existing trips of its area, so if one area has a huge number of deliveries this becomes close to O(n^2). Memory is also a concern because I load the whole file into lists (`Delivery` objects, the skipped list, and the `HashSet` of seen ids), and all of it stays in memory until the end. Sorting is fine (O(n log n)).

**5. What would you improve if you had another day to work on the solution?**

- Speed up trip lookup (for example, keep trips in a structure ordered by remaining space, or use best-fit decreasing).
- Allow mixing nearby areas to fill trips that have a lot of empty space.
- Process the file in a streaming way instead of loading everything in memory, and add a JSON export next to the CSV one.
- Move to Maven/Gradle with JUnit, and use a proper CSV library.
- Add more tests, including a large random input.