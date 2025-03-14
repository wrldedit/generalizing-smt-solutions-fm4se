package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.*;
import java.util.*;

/**
 * BinarySearchIntegerStrategy finds variable bounds using binary search.
 * This strategy:
 * 1. Starts from a known valid value
 * 2. Uses binary search to find bounds efficiently
 * 3. Provides faster convergence for large ranges
 * 
 * Performance characteristics:
 * - Time complexity: O(n * log r) where n is variables and r is range size
 * - Space complexity: O(1) as it only stores current bounds
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public class BinarySearchIntegerStrategy implements IntegerStrategy {
    private static final int INITIAL_RANGE = 100; // Initial search range
    private final SMTConnector connector;

    public BinarySearchIntegerStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public String analyzeIntegers(BoolExpr formula, Set<String> variables) {
        StringBuilder result = new StringBuilder();
        result.append("Binary Search Integer Strategy:\n");
        result.append("Assumptions:\n");
        result.append("- Variables have finite bounds\n");
        result.append("- Initial search range: ±" + INITIAL_RANGE + "\n");
        result.append("- Range expands exponentially until bounds are found\n\n");
        
        result.append("Found variable ranges:\n");
        
        // Analyze each variable independently
        for (String varName : variables) {
            Bounds bounds = findBounds(formula, varName);
            result.append(String.format("%s: [%d, %d]\n", varName, bounds.lower, bounds.upper));
        }
        
        return result.toString();
    }

    /**
     * Finds a rough range using exponential expansion.
     * Returns [lower, upper] bounds for further refinement.
     */
    private int[] findRoughRange(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int step = INITIAL_RANGE;
        int lower = startValue - step;
        int upper = startValue + step;
        
        // Expand range exponentially until we find unsatisfiable bounds
        while (true) {
            boolean lowerSat = isSatisfiable(variable, formula, ctx, lower);
            boolean upperSat = isSatisfiable(variable, formula, ctx, upper);
            
            if (!lowerSat && !upperSat) {
                break;
            }
            
            if (lowerSat) {
                lower -= step;
            }
            if (upperSat) {
                upper += step;
            }
            step *= 2;
        }
        
        return new int[]{lower, upper};
    }

    /**
     * Uses binary search to find the lowest bound.
     */
    private int findLowerBoundBinary(IntExpr variable, BoolExpr formula, Context ctx, int low, int high) {
        while (low < high) {
            int mid = low + (high - low) / 2;
            if (isSatisfiable(variable, formula, ctx, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * Uses binary search to find the highest bound.
     */
    private int findUpperBoundBinary(IntExpr variable, BoolExpr formula, Context ctx, int low, int high) {
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (isSatisfiable(variable, formula, ctx, mid)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return high;
    }

    /**
     * Checks if the formula is satisfiable with the variable set to a specific value.
     */
    private boolean isSatisfiable(IntExpr variable, BoolExpr formula, Context ctx, int value) {
        Solver solver = ctx.mkSolver();
        solver.add(formula);
        solver.add(ctx.mkEq(variable, ctx.mkInt(value)));
        return solver.check() == Status.SATISFIABLE;
    }

    private static class Bounds {
        final int lower;
        final int upper;
        
        Bounds(int lower, int upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }

    private Bounds findBounds(BoolExpr formula, String varName) {
        Context ctx = connector.getContext();
        IntExpr var = ctx.mkIntConst(varName);
        
        // Get a model to use as starting point
        Model model = connector.getSolution(formula);
        if (model == null) {
            return new Bounds(0, 0); // Default if no solution found
        }
        
        // Get starting value from model
        Expr value = model.evaluate(var, true);
        if (value == null || !(value instanceof IntNum)) {
            return new Bounds(0, 0);
        }
        int startValue = ((IntNum)value).getInt();
        
        // First find rough range using exponential expansion
        int[] roughRange = findRoughRange(var, formula, ctx, startValue);
        
        // Then refine bounds using binary search
        int lowerBound = findLowerBoundBinary(var, formula, ctx, roughRange[0], startValue);
        int upperBound = findUpperBoundBinary(var, formula, ctx, startValue, roughRange[1]);
        
        return new Bounds(lowerBound, upperBound);
    }
} 