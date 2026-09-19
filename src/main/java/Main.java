import java.io.File;
import java.util.Scanner;

/**
 * Entry point for the CSV Data Analyzer.
 * If a filename is passed as a command-line argument, it is analyzed
 * immediately and the program exits. Otherwise, the program enters
 * an interactive prompt loop.
 * @author Sboniso Mathebula
 */
public class Main {

    public static void main(String[] args) {

        // -------- Non-interactive mode (file passed as argument) --------
        if (args.length > 0) {
            String path = args[0].trim();

            if (!path.toLowerCase().endsWith(".csv")) {
                System.err.println("Invalid file name. Must end with .csv");
                return;
            }

            File toRead = new File(path);
            if (CsvReader.readCSV(toRead)) {
                System.out.println(ReportPrinter.genReport(
                        CsvReader.list, CsvReader.Cols, CsvReader.rowCount, path));
            } else {
                System.err.println("Error reading " + path);
            }
            return;
        }

        // -------- Interactive mode (no argument) --------
        Scanner in = new Scanner(System.in);
        System.out.println("Please enter the name of the file, with its extension.");
        System.out.println("Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String line = in.nextLine().trim();

            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye.");
                break;
            }

            if (!line.toLowerCase().endsWith(".csv")) {
                System.err.println("Invalid file name. Must end with .csv");
                continue;
            }

            File toRead = new File(line);
            if (CsvReader.readCSV(toRead)) {
                System.out.println(ReportPrinter.genReport(
                        CsvReader.list, CsvReader.Cols, CsvReader.rowCount, line));
            } else {
                System.err.println("Error reading " + line);
            }
        }
        in.close();
    }
}