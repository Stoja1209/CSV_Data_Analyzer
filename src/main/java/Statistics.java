import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Pure static helper class providing statistical operations
 * over a list of mixed-type values. Only numeric values are used
 * for mean, median, min, max, and standard deviation.
 * @author Sboniso Mathebula
 */
public class Statistics {

    // -------- MIN --------
    // Returns the smallest numeric value.
    public static Object getMin(ArrayList<Object> values) {
        if (values.isEmpty()) return null;

        ArrayList<Number> nums = filterNumbers(values);
        if (nums.isEmpty()) return null;

        Number min = nums.get(0);
        double minD = min.doubleValue();

        for (Number n : nums) {
            if (n.doubleValue() < minD) {
                minD = n.doubleValue();
                min = n;
            }
        }
        return min;
    }

    // -------- MAX --------
    // Returns the largest numeric value.
    public static Object getMax(ArrayList<Object> values) {
        if (values.isEmpty()) return null;

        ArrayList<Number> nums = filterNumbers(values);
        if (nums.isEmpty()) return null;

        Number max = nums.get(0);
        double maxD = max.doubleValue();

        for (Number n : nums) {
            if (n.doubleValue() > maxD) {
                maxD = n.doubleValue();
                max = n;
            }
        }
        return max;
    }

    // -------- MEAN --------
    // Arithmetic average of all numeric values.
    public static Object getMean(ArrayList<Object> values) {
        if (values.isEmpty()) return null;

        double total = 0;
        int count = 0;
        for (Object o : values) {
            if (o instanceof Number) {
                total += ((Number) o).doubleValue();
                count++;
            }
        }
        return count == 0 ? null : total / count;
    }

    // -------- MEDIAN --------
    // Middle value of the sorted numeric list.
    public static Object getMedian(ArrayList<Object> values) {
        if (values.isEmpty()) return null;

        ArrayList<Double> nums = new ArrayList<>();
        for (Object o : values) {
            if (o instanceof Number) nums.add(((Number) o).doubleValue());
        }
        if (nums.isEmpty()) return null;

        double[] arr = new double[nums.size()];
        for (int i = 0; i < nums.size(); i++) arr[i] = nums.get(i);
        Arrays.sort(arr);

        int n = arr.length;
        return (n % 2 == 1) ? arr[n / 2]
                            : (arr[n / 2 - 1] + arr[n / 2]) / 2.0;
    }

    // -------- STANDARD DEVIATION (sample, n-1) --------
    public static Object getSTD(ArrayList<Object> values) {
        if (values.isEmpty()) return null;

        ArrayList<Double> nums = new ArrayList<>();
        for (Object o : values) {
            if (o instanceof Number) nums.add(((Number) o).doubleValue());
        }
        if (nums.size() < 2) return 0.0;

        double mean = 0;
        for (double d : nums) mean += d;
        mean /= nums.size();

        double varianceSum = 0;
        for (double d : nums) varianceSum += Math.pow(d - mean, 2);

        return Math.sqrt(varianceSum / (nums.size() - 1));
    }

    // -------- UNIQUE COUNT --------
    public static int getUniqueVal(ArrayList<Object> values) {
        if (values.isEmpty()) return 0;
        return new HashSet<>(values).size();
    }
	// -------- UNIQUE VALUES (sorted list) --------
	// Returns the distinct values in sorted order, capped at `limit`.
	// The second element of the result indicates how many were omitted.
	public static java.util.List<String> getUniqueValueList(ArrayList<Object> values, int limit) {
		java.util.TreeSet<String> unique = new java.util.TreeSet<>();
		for (Object o : values) {
			if (o != null) unique.add(o.toString());
		}
		java.util.List<String> out = new java.util.ArrayList<>();
		int i = 0;
		for (String s : unique) {
			if (i++ >= limit) break;
			out.add(s);
		}
		return out;
	}
	// -------- BOOLEAN COUNTS --------
	// Returns [trueCount, falseCount] for a column of Boolean values.
	public static int[] getBooleanCounts(ArrayList<Object> values) {
		int t = 0, f = 0;
		for (Object o : values) {
			if (o instanceof Boolean) {
				if ((Boolean) o) t++; else f++;
			}
		}
		return new int[] { t, f };
	}

	// Total distinct count 
	public static int getUniqueCount(ArrayList<Object> values) {
		return getUniqueVal(values);
	}

    // -------- HELPER --------
    // Keeps only Number values (drops Strings, Booleans, nulls).
    private static ArrayList<Number> filterNumbers(ArrayList<Object> values) {
        ArrayList<Number> nums = new ArrayList<>();
        for (Object o : values) {
            if (o instanceof Number) nums.add((Number) o);
        }
        return nums;
    }
}