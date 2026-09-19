# CSV Data Analyzer — Technical Documentation

**Version:** 1.0
**Author:** Sboniso Mathebula
**Language:** Java 17+
**Last updated:** 2026

---

## Table of Contents

1. [Purpose](#1-purpose)
2. [Architecture Overview](#2-architecture-overview)
3. [Class-by-Class Reference](#3-class-by-class-reference)
4. [Execution Flow](#4-execution-flow)
5. [Type Detection Rules](#5-type-detection-rules)
6. [Statistics Reference](#6-statistics-reference)
7. [Delimiter Detection](#7-delimiter-detection)
8. [Missing-Value Handling](#8-missing-value-handling)
9. [Report Format](#9-report-format)
10. [Sample Runs](#10-sample-runs)
11. [Edge Cases & How They Are Handled](#11-edge-cases--how-they-are-handled)
12. [Known Limitations](#12-known-limitations)
13. [Roadmap](#13-roadmap)
14. [Testing Strategy](#14-testing-strategy)
15. [Build & Run Reference](#15-build--run-reference)
16. [License](#16-license)

---

## 1. Purpose

CSV Data Analyzer is a from-scratch, dependency-free Java tool that:

- Reads a delimited text file (comma, tab, or semicolon).
- Infers the type of each column.
- Produces a clean statistical report for numeric columns.
- Reports counts of unique values for text columns.
- Reports True/False counts for boolean columns.
- Accounts for missing values explicitly.

It exists to demonstrate data-handling fundamentals: reading, parsing,
type inference, aggregation, and reporting — the same primitives that
libraries like pandas provide, implemented here for clarity.

---

## 2. Architecture Overview

```
                ┌──────────────────────┐
                │        Main          │  ← entry point, arg parsing,
                └──────────┬───────────┘    interactive prompt
                           │
                           ▼
                ┌──────────────────────┐
                │      CsvReader       │  ← reads file, detects delimiter,
                └──────────┬───────────┘    infers types, produces Columns
                           │
                           ▼
                ┌──────────────────────┐
                │       Column         │  ← holds one column: name, type,
                └──────────┬───────────┘    list of non-missing values
                           │
              ┌────────────┴────────────┐
              ▼                         ▼
    ┌──────────────────┐      ┌──────────────────┐
    │   Statistics     │      │  ReportPrinter   │
    │ (pure functions) │      │  (builds text)   │
    └──────────────────┘      └──────────────────┘
```

**Design principles:**

- **Single Responsibility:** each class does one thing.
- **No external dependencies:** pure JDK, so it compiles anywhere.
- **Static, stateless helpers:** `Statistics` holds no state.
- **Separation of parsing from presentation:** `CsvReader` never formats
  output; `ReportPrinter` never reads files.

---

## 3. Class-by-Class Reference

### 3.1 `Main`

**Responsibility:** Program entry point and user interaction.

**Fields:** none.

**Methods:**

| Method | Description |
|---|---|
| `static void main(String[] args)` | If `args.length > 0`, runs non-interactive. Otherwise enters an interactive prompt loop reading filenames from `stdin`. Type `exit` to quit. |

**Behavior notes:**

- Rejects filenames that do not end with `.csv` (case-insensitive).
- After a successful read, calls `ReportPrinter.genReport(...)` and
  prints the result.
- `CsvReader.list`, `CsvReader.Cols`, and `CsvReader.rowCount` are read
  after `readCSV(...)` returns `true`.

---

### 3.2 `CsvReader`

**Responsibility:** Read the file, detect the delimiter, parse each field
into the correct Java type, and populate `Column` objects.

**Static fields:**

| Field | Type | Purpose |
|---|---|---|
| `list` | `ArrayList<Column>` | Parsed columns, available after a successful read. |
| `Cols` | `int` | Number of columns in the header. |
| `rowCount` | `int` | Number of valid data rows read (excludes blank lines and rows with a wrong field count). |

**Methods:**

| Method | Description |
|---|---|
| `static boolean readCSV(File toRead)` | Reads and parses the file. Returns `true` on success. Resets all static state at the start of each call. |
| `private static Object parseStringToType(String input)` | Converts a raw string to `Integer`, `Long`, `Double`, `Boolean`, or `String`. Returns `null` for missing markers. |

**Internal flow:**

1. Validate file existence and non-empty length.
2. Read the header line; detect delimiter from header.
3. Split header into `String[]` with `split(delimRegex, -1)`.
4. Build one empty `Column` per header field.
5. For each subsequent line:
   - Skip blank lines.
   - Split with `-1` so empty fields are preserved.
   - If token count ≠ header column count, skip the row.
   - Otherwise parse each token; non-null results are added to the
     corresponding column.
6. Infer each column's type from the first non-null value in its value
   list (not from the first row alone).
7. Store `list`, `Cols`, `rowCount`.

**Why `split(..., -1)` instead of `StringTokenizer`:**

`StringTokenizer` collapses consecutive delimiters, so a line like
`a,,c` yields 3 tokens (`a`, `c`) instead of 3 (`a`, ``, `c`).
That silently drops rows with empty fields and misaligns column
indices. `String.split(regex, -1)` keeps trailing and empty fields,
so every row has exactly the same shape as the header.

---

### 3.3 `Column`

**Responsibility:** Hold one column's metadata and its non-missing values.

**Fields (all private):**

| Field | Type | Purpose |
|---|---|---|
| `name` | `String` | Column name from the header. |
| `type` | `String` | Detected type: `Integer`, `Long`, `Double`, `Float`, `Boolean`, `String`, or `Unknown`. |
| `values` | `ArrayList<Object>` | Non-missing values only. |

**Methods:**

| Method | Description |
|---|---|
| `setName` / `getName` | Accessors. |
| `setType` / `getType` | Accessors. |
| `addValue(Object)` | Appends a non-null value. |
| `getValue(int)` | Returns value at index, or `null` if out of range. |
| `getValues()` | Returns the backing list. |
| `isNumeric()` | True if type is `Integer`, `Long`, `Double`, or `Float`. |
| `isBoolean()` | True if type is `Boolean`. |

---

### 3.4 `Statistics`

**Responsibility:** Pure static functions computing aggregates over a
list of values. Never mutates input.

**Methods:**

| Method | Returns | Notes |
|---|---|---|
| `getMin(ArrayList<Object>)` | `Object` | Smallest numeric value. Works across mixed Integer/Long/Double by normalizing via `Number.doubleValue()`. Returns `null` if no numerics. |
| `getMax(ArrayList<Object>)` | `Object` | Largest numeric value. Same normalization. |
| `getMean(ArrayList<Object>)` | `Double` | Arithmetic average over numeric values only. |
| `getMedian(ArrayList<Object>)` | `Double` | Middle value of sorted numerics; averages the two middles for even counts. |
| `getSTD(ArrayList<Object>)` | `Double` | Sample standard deviation (`n-1`). Returns `0.0` for a single value. |
| `getUniqueVal(ArrayList<Object>)` | `int` | Distinct values count (uses `HashSet`). |
| `getUniqueValueList(ArrayList<Object>, int limit)` | `List<String>` | Up to `limit` distinct values, sorted alphabetically via `TreeSet`. |
| `getBooleanCounts(ArrayList<Object>)` | `int[]` | Returns `[trueCount, falseCount]`. |
| `filterNumbers(ArrayList<Object>)` *(private)* | `ArrayList<Number>` | Keeps only `Number` instances. |

---

### 3.5 `ReportPrinter`

**Responsibility:** Turn a list of `Column` objects into the final
human-readable text.

**Constant:**

- `UNIQUE_SAMPLE_LIMIT = 5` — maximum distinct values shown per text column.

**Methods:**

| Method | Description |
|---|---|
| `static String genReport(ArrayList<Column>, int numCols, int totalRows, String fileName)` | Builds and returns the full report as a `String`. |
| `private static String format(Object)` | Formats any numeric value to 2 decimals; returns `"N/A"` for `null`; non-numeric types pass through `String.valueOf`. |

**Report structure per type:**

- **NUMERIC:** Type, Count, Missing, Mean, Median, Min, Max, Std Dev.
- **BOOLEAN:** Type, Count, Missing, True, False.
- **TEXT:** Type, Count, Missing, Unique values (`count (sample1, sample2, …, +N more)`).

`Missing` is computed as `totalRows - column.getValues().size()`, using
the true row count from `CsvReader.rowCount` rather than an inferred
maximum from column sizes.

---

## 4. Execution Flow

```
User
 │
 │  (interactive or argv)
 ▼
Main ──► CsvReader.readCSV(File)
             │
             ├─ open scanner
             ├─ read header
             ├─ detect delimiter
             ├─ build Columns
             ├─ parse each row with split(..., -1)
             ├─ infer types from first non-null value per column
             └─ set static list / Cols / rowCount
             │
             ▼
        ReportPrinter.genReport(list, Cols, rowCount, path)
             │
             ├─ for each Column:
             │     NUMERIC  → Statistics.getMean/getMedian/getMin/getMax/getSTD
             │     BOOLEAN  → Statistics.getBooleanCounts
             │     TEXT     → Statistics.getUniqueVal + getUniqueValueList
             │
             └─ return String
             │
             ▼
        System.out.println(...)
```

---

## 5. Type Detection Rules

For each field, `parseStringToType` tries in order:

1. **Missing markers** → `null`
   - Empty string or whitespace
   - `NA`, `N/A`, `null`, `-` (all case-insensitive)
2. **Boolean** — `true` / `false` (case-insensitive)
3. **Integer** — fits in 32-bit signed range
4. **Long** — fits in 64-bit signed range
5. **Double** — IEEE 754 double
6. **Fallback** — the raw `String`

**Column type = type of the first non-null value in the column.**
This means a leading missing or `NA` in the first row does not
misclassify a numeric column.

**Type promotion note:** if a column contains e.g. `1` then `2.5`,
the column is classified as `Integer` (because the first non-null value
was `1`), but the numeric aggregates still work because `Statistics`
converts every `Number` to `double` before comparing or summing.

---

## 6. Statistics Reference

All numeric aggregates operate on the column's non-missing values.

| Statistic | Formula |
|---|---|
| Count | Number of non-missing values |
| Missing | `rowCount - Count` |
| Mean | `Σ xᵢ / n` |
| Median | Middle of sorted values; average of two middles if `n` is even |
| Min | Smallest numeric value |
| Max | Largest numeric value |
| Std Dev | `√( Σ(xᵢ − x̄)² / (n − 1) )` — sample, `n−1` divisor |
| Unique values (text) | Size of `HashSet` of values |
| True / False (boolean) | Counts of each |

**Edge behavior:**

- Empty column → `null` (formatted as `N/A`).
- Single value → Std Dev returns `0.0`.
- `n = 0` for mean → `null`.

---

## 7. Delimiter Detection

Only the **header line** is inspected:

| Check | Delimiter chosen |
|---|---|
| Header contains `\t` | Tab |
| Header contains `;` | Semicolon |
| Otherwise | Comma |

The chosen delimiter is then applied to every row using
`String.split(Pattern.quote(delim), -1)`.

---

## 8. Missing-Value Handling

A field is treated as missing if, after trimming, it is:

- Empty
- `NA` (case-insensitive)
- `N/A` (case-insensitive)
- `null` (case-insensitive)
- `-`

Missing values are **not** stored in the column's value list, so:

- `Count` = size of that list.
- `Missing` = `rowCount − Count`.
- Numeric aggregates simply ignore them.

Because empty fields are preserved by `split(..., -1)`, a row like
`Bob,,180.2,Pretoria` still counts as a real row and the empty field
is correctly attributed to the right column.

---

## 9. Report Format

```
=== CSV Analysis Report ===
File: <path>
Columns: <n>
Rows: <m>


Column: <name>
  Type: NUMERIC
  Count: <n>
  Missing: <m>
  Mean: <two-decimal>
  Median: <two-decimal>
  Min: <two-decimal>
  Max: <two-decimal>
  Std Dev: <two-decimal>

Column: <name>
  Type: BOOLEAN
  Count: <n>
  Missing: <m>
  True: <n>
  False: <n>

Column: <name>
  Type: TEXT
  Count: <n>
  Missing: <m>
  Unique values: <n> (<sample1>, <sample2>, … [, +N more])
```

Line endings are `\r\n` so the report renders cleanly in Windows
`cmd.exe` output.

---

## 10. Sample Runs

### 10.1 `test1.csv` (baseline)

```
=== CSV Analysis Report ===
File: data/test1.csv
Columns: 3
Rows: 5

Column: id
  Type: NUMERIC
  Count: 5
  Missing: 0
  Mean: 3.00
  Median: 3.00
  Min: 1.00
  Max: 5.00
  Std Dev: 1.58

Column: score
  Type: NUMERIC
  Count: 5
  Missing: 0
  Mean: 70.50
  Median: 72.00
  Min: 45.50
  Max: 91.00
  Std Dev: 19.98

Column: grade
  Type: TEXT
  Count: 5
  Missing: 0
  Unique values: 4 (A, B, C, D)
```

### 10.2 `test4.csv` (mixed types, missing values, boolean)

```
=== CSV Analysis Report ===
File: data/test4.csv
Columns: 7
Rows: 15

Column: employee_id
  Type: NUMERIC
  Count: 15
  Missing: 0
  Mean: 1008.00
  Median: 1008.00
  Min: 1001.00
  Max: 1015.00
  Std Dev: 4.47

Column: name
  Type: TEXT
  Count: 15
  Missing: 0
  Unique values: 15 (Alice Johnson, Bob Smith, Charlie Brown,
                    Diana Prince, Eve Adams, +10 more)

Column: department
  Type: TEXT
  Count: 15
  Missing: 0
  Unique values: 3 (Engineering, Finance, Marketing)

Column: salary
  Type: NUMERIC
  Count: 14
  Missing: 1
  Mean: 82500.00
  Median: 81500.00
  Min: 58000.00
  Max: 115000.00
  Std Dev: 17136.33

Column: bonus
  Type: NUMERIC
  Count: 12
  Missing: 3
  Mean: 6500.00
  Median: 5500.00
  Min: 2000.00
  Max: 15000.00
  Std Dev: 4045.20

Column: start_date
  Type: TEXT
  Count: 15
  Missing: 0
  Unique values: 15 (2014-07-15, 2015-09-01, 2016-05-12,
                    2017-01-20, 2017-12-01, +10 more)

Column: active
  Type: BOOLEAN
  Count: 15
  Missing: 0
  True: 11
  False: 4
```

---

## 11. Edge Cases & How They Are Handled

| Case | Handling |
|---|---|
| Empty file | Rejected with a message. |
| File with header but no data rows | Rejected with a message. |
| Blank line inside data | Skipped silently. |
| Row with wrong column count | Skipped silently. |
| Leading missing value in a numeric column | Column still detected as numeric (uses first non-null value). |
| Empty field between two commas | Preserved by `split(..., -1)`, counted as missing. |
| Tab- or semicolon-separated file | Detected from header. |
| Mixed Integer/Double in one column | Aggregates normalize via `double`. |
| Boolean column | Reported as `BOOLEAN` with True/False counts. |
| Date column | Treated as text; unique-value list shown. |
| Very large file | Not streamed — entire file is in memory. |

---

## 12. Known Limitations

1. **No quoted-field support.** `"Doe, John"` will be split on the
   comma. This is documented in the README.
2. **Type inferred from a single value.** If a column starts with a
   number but later contains text, the text values will still be added
   to the column; `Statistics` will skip them for numeric aggregates,
   but `Count` includes them.
3. **Whole-file loading.** Fine for files up to a few megabytes; not
   suitable for very large datasets.
4. **Dates are text.** No date parsing or date-range aggregation.
5. **Row alignment depends on delimiter consistency.** Files that mix
   delimiters will misparse.

---

## 13. Roadmap

Planned improvements, roughly in priority order:

1. **Quoted-field support** — proper CSV grammar instead of `split`.
2. **Streaming reads** — process row-by-row with a callback to
   reduce memory.
3. **Date detection** — recognize ISO-8601 and common locale formats.
4. **Numeric type promotion** — promote a column to `Double` if any
   value parses as a decimal.
5. **JSON output** — `--json` flag emitting a machine-readable report.
6. **Unit tests** — JUnit 5 covering each edge case in §11.
7. **Sorting/filtering flags** — e.g. `--sort <column>` or
   `--where <column>=<value>`.

---

## 14. Testing Strategy

**Current:** manual verification against the four files in `data/`,
checking every statistic against a hand-computed expected value.

**Planned:** a JUnit 5 test class, `AnalyzerTest`, with one test
method per file asserting:

- `CsvReader.rowCount` equals the expected row count.
- Each column's `Count` and `Missing` are correct.
- Numeric values match expected Mean/Median/Min/Max/Std Dev to two
  decimal places.
- Boolean columns report the expected True/False counts.
- Text columns report the expected unique count.

Example skeleton:

```java
@Test
void test4_rowCount() {
    CsvReader.readCSV(new File("data/test4.csv"));
    assertEquals(15, CsvReader.rowCount);
}

@Test
void test4_salary_missing() {
    CsvReader.readCSV(new File("data/test4.csv"));
    Column salary = CsvReader.list.get(3);
    assertEquals(14, salary.getValues().size());
    assertEquals(1, CsvReader.rowCount - salary.getValues().size());
}
```

---

## 15. Build & Run Reference

### 15.1 Compile (any OS)

```bash
javac -d out src/main/java/*.java
```

- `-d out` sends `.class` files to `out/` instead of scattering them
  next to the sources.
- The wildcard `*.java` must be expanded by the shell. On Windows
  `cmd.exe` this works; on PowerShell use `Get-ChildItem`; on Unix
  shells the glob works as-is.

### 15.2 Run Interactively

```bash
java -cp out Main
```

Output:

```
Please enter the name of the file, with its extension.
Type 'exit' to quit.
> data/test4.csv
```

Type `exit` at any prompt to quit.

### 15.3 Run Non-Interactively

```bash
java -cp out Main data/test4.csv
```

- The path is used exactly as given — it must be relative to the
  working directory or absolute.
- The filename must end in `.csv` (case-insensitive).
- On success, prints the report and exits.
- On failure, prints an error to `stderr` and exits.

### 15.4 Windows Launcher (`docs/run.bat`)

Double-clicking `run.bat` performs the following:

1. Moves to the project root (`cd /d "%~dp0.."`).
2. Verifies that a `data/` folder exists.
3. Deletes `out/` if present and recreates it.
4. Compiles all Java files under `src/main/java/`.
5. Lists every `.csv` file in `data/`.
6. Prompts for a filename (no path needed).
7. Runs `java -cp out Main "data\<name>"`.
8. Pauses before closing so the output is readable.

If compilation fails, the script pauses with an error and exits
without running the analyzer.

### 15.5 Environment Requirements

| Requirement | Version |
|---|---|
| JDK | 17 or higher |
| Shell | Windows `cmd.exe` (for `run.bat`) or any shell (manual) |
| Encoding | UTF-8 (default for `Scanner`) |

---

## 16. Data Model

This section describes how a CSV file maps onto the in-memory model.

### 16.1 File → Columns

```
CSV file
   │
   ├─ header row → Column[0].name, Column[1].name, …
   │
   └─ data rows  → for each field:
                     parseStringToType(field)
                     if result != null:
                         Column[i].addValue(result)
```

Each `Column` keeps only non-missing values. Missing-ness is thus
implicit in the difference between `rowCount` and
`Column.values.size()`.

### 16.2 Value Type Hierarchy

```
Object
 ├── Integer        (from Integer.parseInt)
 ├── Long           (from Long.parseLong)
 ├── Double         (from Double.parseDouble)
 ├── Boolean        (from Boolean.parseBoolean)
 └── String         (fallback)
```

Statistics only operate on `Number` subtypes. Booleans and Strings
are counted or listed, never summed.

### 16.3 Type Predicate Matrix

| Detected type | `isNumeric()` | `isBoolean()` | Report branch |
|---|---|---|---|
| `Integer` | ✅ | ❌ | NUMERIC |
| `Long` | ✅ | ❌ | NUMERIC |
| `Double` | ✅ | ❌ | NUMERIC |
| `Float` | ✅ | ❌ | NUMERIC |
| `Boolean` | ❌ | ✅ | BOOLEAN |
| `String` | ❌ | ❌ | TEXT |
| `Unknown` | ❌ | ❌ | TEXT |

`Float` is listed for completeness — the current parser never
produces it (`Double.parseDouble` accepts all float-range values),
but the predicate allows it in case a future version adds it.

`Unknown` occurs only for columns whose every value is missing. Such
columns render as TEXT with `Count: 0` and `Unique values: 0`.

---

## 17. Public API Reference

Although this is a command-line application, the classes are
independently usable. Below is the stable surface.

### 17.1 `CsvReader`

```java
public static boolean readCSV(File toRead)
```

- **Returns:** `true` on success, `false` on any failure.
- **Side effects:** populates `CsvReader.list`, `CsvReader.Cols`,
  `CsvReader.rowCount`.
- **Error handling:** failures are reported on `System.err`. Stack
  traces are printed for unexpected exceptions.
- **Repeatability:** the method resets its static state at the start
  of each call, so calling it twice in one process is safe.

```java
public static ArrayList<Column> list   // parsed columns, or null before first read
public static int Cols                  // number of columns in the header
public static int rowCount              // number of valid data rows
```

### 17.2 `Column`

```java
public String getName()
public void setName(String name)
public String getType()
public void setType(String type)
public void addValue(Object value)
public Object getValue(int index)
public ArrayList<Object> getValues()
public boolean isNumeric()
public boolean isBoolean()
```

### 17.3 `Statistics`

All methods are `static` and side-effect-free.

```java
public static Object getMin(ArrayList<Object> values)
public static Object getMax(ArrayList<Object> values)
public static Object getMean(ArrayList<Object> values)
public static Object getMedian(ArrayList<Object> values)
public static Object getSTD(ArrayList<Object> values)
public static int    getUniqueVal(ArrayList<Object> values)
public static List<String> getUniqueValueList(ArrayList<Object> values, int limit)
public static int[]  getBooleanCounts(ArrayList<Object> values)
```

### 17.4 `ReportPrinter`

```java
public static String genReport(ArrayList<Column> list,
                               int numCols,
                               int totalRows,
                               String fileName)
```

Returns the complete report as a single String. No `System.out`
calls happen inside — the caller decides where to print.

---

## 18. Design Decisions & Rationale

This section explains *why* the code looks the way it does. Useful
for reviewers and future maintainers.

### 18.1 Why plain `javac` and not Maven/Gradle?

- **Teaching context.** The project is meant to illustrate the
  compile-run cycle without build-tool magic.
- **Zero setup.** No downloads, no plugin resolution, no cache.
- **Portable.** A single `javac -d out src/main/java/*.java` works on
  any machine with a JDK.

Trade-off: no dependency management. If the project grows, a
`pom.xml` or `build.gradle` becomes worthwhile.

### 18.2 Why static state in `CsvReader`?

`list`, `Cols`, and `rowCount` are static so `Main` can call
`readCSV(...)` once and then read the results. This mirrors how small
CLI tools often pass state without ceremony.

Trade-off: not thread-safe. For concurrent use, the method should
return a result object instead.

### 18.3 Why `split(regex, -1)` and not a CSV library?

The project intentionally avoids external dependencies. `split` with
limit `-1` handles the common cases (no quotes, no embedded
delimiters) correctly. A proper CSV parser is on the roadmap.

### 18.4 Why normalize numeric comparisons to `double`?

Java's `Number` hierarchy has no shared `compareTo` for mixed
subtypes. A column may contain `Integer 1` then `Double 2.5`. The
simplest correct comparison is `Number.doubleValue()`. It trades a
tiny amount of precision for uniform behavior.

### 18.5 Why two report signatures changed over time?

`genReport` gained `totalRows` because column sizes cannot reliably
recover row count when every column has a missing value. Passing the
count explicitly makes the data flow obvious and testable.

### 18.6 Why not use `Scanner` with a custom delimiter?

`Scanner.useDelimiter` also collapses nothing, but its tokenizer
handles line boundaries in ways that surprise readers. `split` is
more transparent and easier to reason about.

---

## 19. Extending the Project

This section outlines how to add common features without rewriting
the pipeline.

### 19.1 Adding a New Statistic (e.g. Range)

1. Add a method to `Statistics`:

   ```java
   public static Object getRange(ArrayList<Object> values) {
       Number min = (Number) getMin(values);
       Number max = (Number) getMax(values);
       if (min == null || max == null) return null;
       return max.doubleValue() - min.doubleValue();
   }
   ```

2. Call it from `ReportPrinter`'s NUMERIC branch:

   ```java
   sb.append("Range: ").append(format(Statistics.getRange(c.getValues()))).append("\r\n  ");
   ```

3. Add a matching note to README and DOCUMENTATION.

### 19.2 Adding a New Value Type (e.g. `LocalDate`)

1. In `parseStringToType`, add a branch:

   ```java
   try { return java.time.LocalDate.parse(input); }
   catch (java.time.format.DateTimeParseException ignored) {}
   ```

2. Add an `isDate()` predicate to `Column`.

3. Add a DATE branch to `ReportPrinter` (e.g. show earliest and
   latest values).

4. Update the type table in §16.3.

### 19.3 Adding a New Report Format (e.g. JSON)

1. Create a `JsonReportPrinter` class.
2. Add a flag in `Main` (e.g. `--json`) that routes to the new
   printer.
3. Keep `ReportPrinter.genReport` untouched.

This preserves the single-responsibility split between parsing and
presentation.

### 19.4 Adding CLI Flags

Currently the only "flag" is a positional argument. To add real
options:

1. Parse `args` in `Main` into a small `Options` object.
2. Support `--json`, `--sort <col>`, `--where <col>=<value>`, etc.
3. Validate flag combinations and print usage on error.

Example:

```java
Options opts = Options.parse(args);
if (opts == null) { printUsage(); return; }
```

### 19.5 Adding Unit Tests

Create `src/test/java/AnalyzerTest.java` (or run manually via a
`main` method if you don't want to add a test framework). See §14
for a testing strategy and sample assertions.

---

## 20. Glossary

| Term | Meaning |
|---|---|
| **Column** | A named vertical slice of a CSV file, corresponding to one header field. |
| **Row** | One line of data. In this project, a row is valid if its field count matches the header. |
| **Missing value** | A field that is empty or equal to a recognized marker (`NA`, `N/A`, `null`, `-`). |
| **Detected type** | The Java class name of the first non-null value in a column: `Integer`, `Long`, `Double`, `Boolean`, or `String`. |
| **Numeric column** | A column whose detected type is `Integer`, `Long`, `Double`, or `Float`. |
| **Boolean column** | A column whose detected type is `Boolean`. |
| **Text column** | Any column that is not numeric and not boolean. Includes dates and free text. |
| **Sample std dev** | Standard deviation with an `n-1` divisor; used as the default in this project. |
| **Delimiter** | The character separating fields: `,`, `\t`, or `;`. |

---

## 21. FAQ

**Q: Why does a numeric column sometimes print Min without decimals
but Mean with decimals?**
A: That was a bug fixed in the numeric formatting commit. As of
version 1.0, `format()` promotes every `Number` to `double`, so all
numeric lines print two decimals consistently.

**Q: Why is my date column reported as TEXT?**
A: Dates are intentionally left as text in this version. See the
roadmap for a planned date-detection feature.

**Q: Why are some rows missing from my file's report?**
A: Two possibilities:

1. The row's field count did not match the header. Malformed rows
   are skipped silently.
2. The row was blank and got skipped.

Check the file for stray commas inside values (e.g. `Smith, John`
without quotes) or mixed delimiters.

**Q: Why does a column with a leading `NA` still count as numeric?**
A: Type is inferred from the first *non-null* value across the whole
column. So a numeric value further down correctly drives the type.

**Q: What happens if two columns have the same name?**
A: They are treated as two separate `Column` objects. The report
prints both, one after the other, with the same name. Nothing is
merged or deduplicated.

**Q: What happens if the file uses a delimiter not in the detected
set (e.g. `|`)?**
A: The header is scanned for tab or semicolon; if neither is found,
comma is assumed. A pipe-delimited file will be read as a single
column. Support for additional delimiters is a roadmap item.

**Q: Can I pipe input into the tool?**
A: No. The tool opens a file path. `Main` reads filenames from
`stdin`, not data.

---

## 22. Changelog

### v1.0 — Current

**Fixes**

- Correctly preserve empty fields using `split(..., -1)` (previously
  `StringTokenizer` collapsed them and silently dropped rows).
- Track true row count in `CsvReader.rowCount`; use it for `Rows` and
  `Missing`.
- Infer column type from the first non-null value across the entire
  column (was: first value of the first row).
- Normalize numeric comparisons via `Number.doubleValue()` to prevent
  `ClassCastException` on mixed Integer/Double columns.
- Format all numeric statistics to two decimals consistently
  (previously only `Double` and `Float` were formatted).

**Features**

- Added `Statistics.getUniqueValueList(...)` and sample values in the
  TEXT branch.
- Added `Statistics.getBooleanCounts(...)` and a dedicated BOOLEAN
  branch in the report.
- Encapsulated `Column` fields with getters and added `isNumeric()`
  and `isBoolean()` predicates.

**Docs**

- Rewrote `README.txt` to match the new report format.
- Added this `DOCUMENTATION.md`.

### Planned for v1.1

- Quoted-field support.
- Streaming reads.
- Unit tests (JUnit 5).
- JSON output flag.

---

## 23. References

- **Java SE 17 API** — `java.util.Scanner`, `java.lang.String#split`,
  `java.util.regex.Pattern`.
- **Java `Number` hierarchy** — `Integer`, `Long`, `Double`, `Float`.
- **RFC 4180** — the informal CSV specification. This project follows
  the subset described in §5.
- **Sample standard deviation** — see any introductory statistics
  text; the `n-1` divisor is used to make the estimator unbiased.

---

## 24. License

This project is released under the MIT License.

```
MIT License

Copyright (c) 2026 Sboniso Mathebula

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject
to the following conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 25. Contact

For questions, bug reports, or suggestions:

- **GitHub Issues:**
  `https://github.com/Stoja1209/CSV_Data_Analyzer/issues`
- **Author:** Sboniso Mathebula

Please include the file being analyzed (or a minimal reproduction)
and the full console output when reporting a bug.

---

**END OF DOCUMENTATION**