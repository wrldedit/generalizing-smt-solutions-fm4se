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
    public String analyzeIntegers(BoolExpr formula, Set<String> variableNames) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Binary Search Integer Strategy:\n");

        // Get a model to use as starting point
        Model model = connector.getSolution(formula);
        if (model == null) {
            return "No solution found for the formula.";
        }

        // Analyze each variable
        for (String varName : variableNames) {
            IntExpr variable = ctx.mkIntConst(varName);
            
            // Get starting value from model
            Expr value = model.evaluate(variable, true);
            if (value == null || !(value instanceof IntNum)) {
                continue;
            }
            int startValue = ((IntNum)value).getInt();

            // First find rough range using exponential expansion
            int[] roughRange = findRoughRange(variable, formula, ctx, startValue);
            
            // Then refine bounds using binary search
            int lowerBound = findLowerBoundBinary(variable, formula, ctx, roughRange[0], startValue);
            int upperBound = findUpperBoundBinary(variable, formula, ctx, startValue, roughRange[1]);
            
            result.append(String.format("%s: [%d, %d]\n", varName, lowerBound, upperBound));
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
            
            // Prevent integer overflow by checking bounds
            if (lowerSat) {
                // Check if next step would cause overflow
                if (lower > Integer.MIN_VALUE + step) {
                    lower -= step;
                } else {
                    lower = Integer.MIN_VALUE;
                }
            }
            if (upperSat) {
                // Check if next step would cause overflow
                if (upper < Integer.MAX_VALUE - step) {
                    upper += step;
                } else {
                    upper = Integer.MAX_VALUE;
                }
            }
            
            // Double step size but prevent overflow
            if (step <= Integer.MAX_VALUE / 2) {
                step *= 2;
            } else {
                // If we can't double step anymore, we've reached maximum range
                break;
            }
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
} 