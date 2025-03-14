package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.*;

/**
 * BinarySearchIntervalStrategy finds variable bounds using binary search.
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
public class BinarySearchIntervalStrategy extends AbstractIntervalStrategy {
    private static final int INITIAL_RANGE = 100; // Initial search range

    /**
     * Creates a strategy that analyzes all integer variables.
     *
     * @param connector the SMTConnector providing the Z3 context
     */
    public BinarySearchIntervalStrategy(SMTConnector connector) {
        super(connector);
    }

    /**
     * Creates a strategy that analyzes a specific variable.
     *
     * @param connector the SMTConnector providing the Z3 context
     * @param variableName name of the variable to analyze
     */
    public BinarySearchIntervalStrategy(SMTConnector connector, String variableName) {
        super(connector, variableName);
    }

    @Override
    protected String getStrategyName() {
        return "Binary Search Interval Strategy";
    }

    @Override
    protected Interval findInterval(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        // First find a rough range using exponential expansion
        int[] roughRange = findRoughRange(variable, formula, ctx, startValue);
        
        // Then refine the bounds using binary search
        int lowerBound = findLowerBoundBinary(variable, formula, ctx, roughRange[0], startValue);
        int upperBound = findUpperBoundBinary(variable, formula, ctx, startValue, roughRange[1]);
        
        return new Interval(lowerBound, upperBound);
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
} 