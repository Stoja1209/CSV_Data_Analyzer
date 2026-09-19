import java.util.ArrayList;

/**
 * Builds the human-readable text report from a list of parsed Columns.
 * Numeric columns get Mean/Median/Min/Max/Std Dev.
 * Text columns get a unique-value count.
 * @author Sboniso Mathebula
 */
public class ReportPrinter {

    public static String genReport(ArrayList<Column> list, int numCols,
                                   int totalRows, String fileName) {
        StringBuilder sb = new StringBuilder();

        sb.append("=== CSV Analysis Report ===\r\n");
        sb.append("File: ").append(fileName).append("\r\n");
        sb.append("Columns: ").append(numCols).append("\r\n");
        sb.append("Rows: ").append(totalRows).append("\r\n\r\n\r\n");

        // Print each column
        for (Column c : list) {
            String type = c.getType();
            sb.append("Column: ").append(c.getName()).append("\r\n").append("  ");
			int count = c.getValues().size();
            int missing = totalRows - count;
				
            if (c.isNumeric()) {

                sb.append("Type: NUMERIC\r\n  ");
                sb.append("Count: ").append(count).append("\r\n  ");
                sb.append("Missing: ").append(missing).append("\r\n  ");
                sb.append("Mean: ").append(format(Statistics.getMean(c.getValues()))).append("\r\n  ");
                sb.append("Median: ").append(format(Statistics.getMedian(c.getValues()))).append("\r\n  ");
                sb.append("Min: ").append(format(Statistics.getMin(c.getValues()))).append("\r\n  ");
                sb.append("Max: ").append(format(Statistics.getMax(c.getValues()))).append("\r\n  ");
                sb.append("Std Dev: ").append(format(Statistics.getSTD(c.getValues()))).append("\r\n\r\n");
				
            }else if (c.isBoolean()) {

				int[] counts = Statistics.getBooleanCounts(c.getValues());

				sb.append("Type: BOOLEAN\r\n  ");
				sb.append("Count: ").append(count).append("\r\n  ");
				sb.append("Missing: ").append(missing).append("\r\n  ");
				sb.append("True: ").append(counts[0]).append("\r\n  ");
				sb.append("False: ").append(counts[1]).append("\r\n\r\n");
			} else {

				int uniqueCount = Statistics.getUniqueVal(c.getValues());

				sb.append("Type: TEXT\r\n  ");
				sb.append("Count: ").append(count).append("\r\n  ");
				sb.append("Missing: ").append(missing).append("\r\n  ");
				sb.append("Unique values: ").append(uniqueCount);

				// Show up to 5 unique values inline
				if (uniqueCount > 0) {
					int limit = 5;
					java.util.List<String> sample = Statistics.getUniqueValueList(c.getValues(), limit);
					sb.append(" (").append(String.join(", ", sample));
					if (uniqueCount > limit) {
						sb.append(", +").append(uniqueCount - limit).append(" more");
					}
					sb.append(")");
				}
				sb.append("\r\n\r\n");
				}
        }

        return sb.toString();
    }

	// Formats any numeric value to 2 decimal places for a clean report.
	private static String format(Object o) {
		if (o == null) return "N/A";
		if (o instanceof Number) {
			return String.format("%.2f", ((Number) o).doubleValue());
		}
		return String.valueOf(o);
	}
}