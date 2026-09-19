import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Reads a CSV file, detects its delimiter (comma, tab, semicolon),
 * determines each column's type, and stores the values in a list of Columns.
 * Missing values (blank, NA, N/A, null, -) are skipped.
 * @author Sboniso Mathebula
 */
public class CsvReader {

    // The parsed columns, available to other classes after readCSV() succeeds
    static ArrayList<Column> list;

    // Number of columns in the header
    static int Cols;

    // Number of data rows successfully read (excludes blanks and malformed rows)
    static int rowCount;

    /**
     * Reads the given CSV file.
     * @return true if reading succeeded, false otherwise
     */
    public static boolean readCSV(File toRead) {

        // Reset state so repeated calls don't carry stale data
        list = null;
        Cols = 0;
        rowCount = 0;

        // Basic checks
        if (!toRead.exists() || toRead.length() == 0) {
            System.err.println("Either the file is empty or it doesn't exist.");
            return false;
        }

        try (Scanner in = new Scanner(toRead)) {

            // Read the header line
            if (!in.hasNextLine()) {
                System.err.println("File has no header line.");
                return false;
            }
            String header = in.nextLine().trim();

            // Auto-detect delimiter: tab > semicolon > comma (default)
            char delimiter = ',';
            if (header.contains("\t")) delimiter = '\t';
            else if (header.contains(";")) delimiter = ';';

            // split() preserves empty fields
            String delimRegex = Pattern.quote(String.valueOf(delimiter));

            String[] headerTokens = header.split(delimRegex, -1);
            int numCols = headerTokens.length;
            Cols = numCols;

            // Build one Column per header field
            ArrayList<Column> colList = new ArrayList<>();
            for (String colName : headerTokens) {
                Column col = new Column();
                col.setName(colName);
                colList.add(col);
            }

            // Read every data row
            int rows = 0;
            while (in.hasNextLine()) {
                String read = in.nextLine();

                // Skip empty lines
                if (read.trim().isEmpty()) continue;

                // split(..., -1) keeps empty fields, so a missing value
                // still occupies its slot and stays aligned with the header.
                String[] tokens = read.split(delimRegex, -1);

                // Skip malformed rows
                if (tokens.length != numCols) continue;

                rows++;
                for (int i = 0; i < numCols; i++) {
                    Object obj = parseStringToType(tokens[i]);
                    if (obj != null) {
                        colList.get(i).addValue(obj);
                    }
                }
            }

            if (rows == 0) {
                System.err.println("File has headers but no data rows.");
                return false;
            }

            // Infer column types
            // Using the first non-null value found across the whole column
            for (Column c : colList) {
                if (c.getValues().isEmpty()) {
                    c.setType("Unknown");
                } else {
                    c.setType(c.getValues().get(0).getClass().getSimpleName());
                }
            }

            list = colList;
            rowCount = rows;
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Parses a string into the most specific type possible.
     * Returns null for missing markers (blank, NA, N/A, null, -).
     * Tries Integer, Long, Double, Boolean, then falls back to String.
     */
    private static Object parseStringToType(String input) {
        if (input == null || input.trim().isEmpty()) return null;
        input = input.trim();

        // Missing markers
        if (input.equalsIgnoreCase("NA")
                || input.equalsIgnoreCase("N/A")
                || input.equalsIgnoreCase("null")
                || input.equals("-")) {
            return null;
        }

        // Boolean
        if (input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(input);
        }

        // Integer
        try { return Integer.parseInt(input); } catch (NumberFormatException ignored) {}

        // Long
        try { return Long.parseLong(input); } catch (NumberFormatException ignored) {}

        // Double
        try { return Double.parseDouble(input); } catch (NumberFormatException ignored) {}

        // Fallback
        return input;
    }
}