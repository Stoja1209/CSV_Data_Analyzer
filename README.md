
                           CSV DATA ANALYZER                                 
                    Command-Line Statistical Tool                             
                                                                              
                 Read CSV files. Detect types. Report stats.                                                                                                


-------------------------------------------------------------------------------
1. PROJECT OVERVIEW
-------------------------------------------------------------------------------

CSV Data Analyzer is a lightweight Java command-line tool that reads any CSV
file, detects the type of every column, and produces a clean statistical
report on the numeric columns inside it.

It was written to build foundational data-handling skills that matter for
machine learning and data engineering — the kind of work that sits behind
libraries like pandas, but implemented here from scratch for clarity.

Key Features:
- Auto-detects delimiter (comma, tab, semicolon)
- Detects column types (Integer, Long, Double, Float, Boolean, String)
- Correctly handles empty fields and missing markers
  (blank, NA, N/A, null, -)
- Computes Count, Missing, Mean, Median, Min, Max, Std Dev
- Reports unique value counts for text columns, with a short sample
- Recognizes boolean columns and prints a True / False breakdown
- Zero external dependencies — pure JDK
- Simple batch launcher for Windows
- IDE-agnostic (compiles with plain javac)


-------------------------------------------------------------------------------
2. TECHNOLOGY STACK
-------------------------------------------------------------------------------

Language:     Java 17+
Build:        Plain javac (no Maven / Gradle required)
Testing:      Manual + JUnit 5 (planned)
Editor:       Any (VS Code, IntelliJ, Eclipse, Notepad++)
Version Ctrl: Git & GitHub
Platform:     Windows (batch launcher) / any OS (manual run)


-------------------------------------------------------------------------------
3. PROJECT STRUCTURE
-------------------------------------------------------------------------------

csv-data-analyzer/
│
├── data/                             ← Place your CSV files here
│   ├── test1.csv                     ← Small, clean, numeric
│   ├── test2.csv                     ← Mixed with one missing value
│   ├── test3.csv                     ← Tab-separated with multiple gaps
│   └── test4.csv                     ← Larger, mixed types, realistic
│
├── docs/                             ← Launcher & documentation
│   ├── run.bat                       ← Windows launcher
│   └── DOCUMENTATION.md              ← Full technical docs
│
├── src/                              ← Java source (standard layout)
│   └── main/
│       └── java/
│           ├── Main.java
│           ├── CsvReader.java
│           ├── Column.java
│           ├── Statistics.java
│           └── ReportPrinter.java
│
├── out/                              ← Compiled classes (auto-generated)
├── .gitignore                        ← Git ignore rules
├── README.txt                        ← This file
└── LICENSE


-------------------------------------------------------------------------------
4. INSTALLATION & SETUP
-------------------------------------------------------------------------------

Prerequisites:
- Java Development Kit (JDK) 17 or higher
- Windows (for run.bat) or any OS for the manual method

Step 1: Clone the Repository
----------------------------------------------------------
git clone https://github.com/Stoja1209/CSV_Data_Analyzer.git

cd CSV-Data-Analyzer

Step 2: Place Your Data
----------------------------------------------------------
Put at least one .csv file inside the "data/" folder.

Step 3: Run (Windows)
----------------------------------------------------------
1. Open the "docs/" folder
2. Double-click run.bat
3. When prompted, type the filename only (e.g. test2.csv)
4. The script compiles the project and prints the report

Step 3 (Manual - any OS)
----------------------------------------------------------
javac -d out src/main/java/*.java
java -cp out Main
# When prompted: data/test2.csv

Type "exit" to quit.


-------------------------------------------------------------------------------
5. HOW TO USE THE APPLICATION
-------------------------------------------------------------------------------

1. Pick a CSV file and place it inside the "data/" folder.
2. Run the program (batch file or manual).
3. Enter the filename (no path needed if using run.bat).
4. Read the report printed to the console.

The report contains one section per column:

  Column: <name>
    Type: NUMERIC | BOOLEAN | TEXT
    Count: <number of non-missing values>
    Missing: <number of missing values>

    # If NUMERIC:
    Mean: <two-decimal average>
    Median: <two-decimal middle value>
    Min: <two-decimal smallest>
    Max: <two-decimal largest>
    Std Dev: <two-decimal sample std deviation>

    # If BOOLEAN:
    True: <count of true values>
    False: <count of false values>

    # If TEXT:
    Unique values: <count of distinct entries> (<up to 5 samples> [, +N more])


-------------------------------------------------------------------------------
6. SAMPLE DATA FILE
-------------------------------------------------------------------------------

Below is the sample CSV used in the example output (test2.csv):

  product,price,quantity,sold
  Laptop,12000.50,15,yes
  Mouse,250.00,120,no
  Keyboard,850.75,45,yes
  Monitor,3200.00,20,yes
  Headphones,1500.25,60,no
  Webcam,NA,30,yes

Note: "NA" in the price column is treated as missing and excluded from
the numeric statistics.


-------------------------------------------------------------------------------
7. EXAMPLE OUTPUT
-------------------------------------------------------------------------------

=== CSV Analysis Report ===
File: data/test2.csv
Columns: 4
Rows: 6


Column: product
  Type: TEXT
  Count: 6
  Missing: 0
  Unique values: 6 (Headphones, Keyboard, Laptop, Monitor, Mouse, +1 more)

Column: price
  Type: NUMERIC
  Count: 5
  Missing: 1
  Mean: 3560.30
  Median: 1500.25
  Min: 250.00
  Max: 12000.50
  Std Dev: 4845.37

Column: quantity
  Type: NUMERIC
  Count: 6
  Missing: 0
  Mean: 48.33
  Median: 37.50
  Min: 15.00
  Max: 120.00
  Std Dev: 38.82

Column: sold
  Type: TEXT
  Count: 6
  Missing: 0
  Unique values: 2 (no, yes)


-------------------------------------------------------------------------------
8. TEST DATA SETS
-------------------------------------------------------------------------------

Four test files are included in the "data/" folder. Each one targets a
different edge case.

 test1.csv — Tiny, clean, all-numeric except one text column                  
 Purpose: Verify baseline correctness on the simplest possible input.         

  id,score,grade
  1,45.5,C
  2,72.0,B
  3,88.5,A
  4,91.0,A
  5,55.5,D

  Rows: 5   Columns: 3
  Tests: mean, median, min, max, stddev, unique text values


 test2.csv — Mixed types with a single missing value (shown above)            
 Purpose: Verify missing-value handling and mixed Integer/Double columns.     

  product,price,quantity,sold
  Laptop,12000.50,15,yes
  Mouse,250.00,120,no
  Keyboard,850.75,45,yes
  Monitor,3200.00,20,yes
  Headphones,1500.25,60,no
  Webcam,NA,30,yes

  Rows: 6   Columns: 4
  Tests: NA handling, mixed Integer/Double, text vs numeric classification


 test3.csv — Tab-separated with multiple missing values                       
 Purpose: Verify delimiter auto-detection and sparse data.                    

  (tab)name( tab)age(tab)height(tab)city
  Alice   23   165.5   Johannesburg
  Bob            180.2   Pretoria
  Charlie 30           Durban
  Diana   27   170.0
  Eve            155.8   Cape Town

  Rows: 5   Columns: 4
  Tests: tab detection, blank-field handling, partial rows



 test4.csv — Larger dataset with mixed types and multiple edge cases          
 Purpose: Stress-test the analyzer on realistic, messy data.                  

  employee_id,name,department,salary,bonus,start_date,active
  1001,Alice Johnson,Engineering,85000.00,5000.00,2019-03-15,true
  1002,Bob Smith,Marketing,62000.00,,2018-07-01,true
  1003,Charlie Brown,Engineering,91000.00,7500.00,2017-01-20,false
  1004,Diana Prince,Finance,78000.00,3000.00,2020-11-05,true
  1005,Eve Adams,Engineering,95000.00,8500.00,2016-05-12,true
  1006,Frank Castle,Marketing,,2000.00,2021-02-18,false
  1007,Grace Hopper,Engineering,105000.00,12000.00,2015-09-01,true
  1008,Henry Ford,Finance,71000.00,,2019-06-30,true
  1009,Ivy Chen,Marketing,66000.00,4000.00,2020-08-14,true
  1010,Jack Ryan,Engineering,88000.00,6000.00,2018-04-22,false
  1011,Karen Page,Finance,73000.00,,2021-05-10,true
  1012,Leo Messi,Engineering,115000.00,15000.00,2014-07-15,true
  1013,Mona Lisa,Marketing,58000.00,2500.00,2022-01-03,true
  1014,Nina Simone,Finance,69000.00,3500.00,2019-09-19,false
  1015,Oscar Wilde,Engineering,99000.00,9000.00,2017-12-01,true

  Rows: 15   Columns: 7
  Tests: large numeric ranges, missing salary and bonus, boolean column,
         multiple text categories, date column left as text


-------------------------------------------------------------------------------
9. HOW THE DELIMITER IS DETECTED
-------------------------------------------------------------------------------

The reader inspects the header line:

  1. If it contains a TAB character     → uses TAB
  2. Else if it contains a semicolon    → uses semicolon
  3. Else                               → uses comma (default)

The detected delimiter is then used for every row via String.split(delim, -1).
The "-1" limit preserves empty fields, so a missing value still occupies
its own slot and stays aligned with the header. This is what allows the
analyzer to correctly count missing values per column.

This means a file saved by Excel or a text editor will still be read
correctly regardless of its separator.


-------------------------------------------------------------------------------
10. TYPE DETECTION RULES
-------------------------------------------------------------------------------

Each field is parsed in the following order:

  1. Missing markers → null
       blank, "NA", "N/A", "null", "-"   (case-insensitive)

  2. Boolean  → true / false   (case-insensitive)
  3. Integer  → e.g. 42
  4. Long     → e.g. 1234567890123
  5. Double   → e.g. 3.14
  6. Fallback → String

A column's type is the type of the first non-missing value found anywhere
in that column — not just the first row — so a leading empty or "NA" cell
will not misclassify the whole column.


-------------------------------------------------------------------------------
11. KNOWN LIMITATIONS
-------------------------------------------------------------------------------

- No support for quoted fields (e.g. "Doe, John" would split on comma)
- Column type is inferred from the first non-missing value only
- Entire file is loaded into memory (fine for files up to a few MB)
- Date columns are treated as text
- Rows with a column count that differs from the header are skipped

Planned improvements are listed in the Roadmap in DOCUMENTATION.md.


-------------------------------------------------------------------------------
12. TROUBLESHOOTING
-------------------------------------------------------------------------------

Issue: "The system cannot find the path specified"
Solution: Ensure you are running run.bat from the "docs/" folder. The script
          automatically moves one level up to the project root.

Issue: "javac is not recognized as an internal or external command"
Solution: Install the JDK and add it to your PATH environment variable.

Issue: "File not found: data/<name>.csv"
Solution: Make sure the CSV file is inside the "data/" folder and the
          filename is spelled exactly as it appears on disk.

Issue: "Rows don't have equal columns"
Solution: A row in your CSV has a different number of fields than the header.
          That row is skipped automatically. Check the file for stray
          delimiters or unescaped commas inside values.

Issue: Unexpected output for a numeric column
Solution: If the first non-missing value in that column was non-numeric
          (e.g. "N/A" then "abc"), the column is classified as text.
          Clean your data or reorder so a numeric value appears first.


-------------------------------------------------------------------------------
13. CONTRIBUTORS
-------------------------------------------------------------------------------

| Name             | Student Number | Role                              |
|------------------|----------------|-----------------------------------|
| MATHEBULA S      | 225150529      | Author / Developer                |

Role Breakdown:
- Author / Developer: Designed, implemented, tested, and documented the project.


-------------------------------------------------------------------------------
14. LICENSE
-------------------------------------------------------------------------------

This project was developed for personal and academic learning purposes.

MIT License
Copyright (c) 2026 Sboniso Mathebula


-------------------------------------------------------------------------------
15. CONTACT
-------------------------------------------------------------------------------

For any questions or issues, please contact:
- GitHub Issues: https://github.com/Stoja1209/CSV_Data_Analyzer/issues

-------------------------------------------------------------------------------
END OF README
-------------------------------------------------------------------------------
