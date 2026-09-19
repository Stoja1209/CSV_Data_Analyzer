import java.util.ArrayList;

/**
 * Represents a single column in a CSV file.
 * Holds the column name, its detected type (e.g. Integer, Double, String),
 * and all the values read from that column (missing values are skipped).
 * @author Sboniso Mathebula
 */
public class Column {

    // Column name as it appears in the header row
    private String name;

    // Detected type based on the first non-null value (e.g. "Integer", "String")
    private String type;

    // List of all non-missing values in this column
    private ArrayList<Object> values;

    public Column() {
        values = new ArrayList<>();
        type = "Unknown";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    // Adds a non-null value to this column
    public void addValue(Object value) {
        this.values.add(value);
    }

    // Returns the value at the given index, or null if out of bounds
    public Object getValue(int index) {
        if (index >= 0 && index < this.values.size()) {
            return this.values.get(index);
        }
        return null;
    }

    public ArrayList<Object> getValues() {
        return this.values;
    }

    // Returns true if this column holds numeric data
    public boolean isNumeric() {
        return type.equals("Integer") || type.equals("Long")
                || type.equals("Double") || type.equals("Float");
    }
	
	// Returns true if this column holds boolean data
	public boolean isBoolean() {
		return type.equals("Boolean");
	}
}