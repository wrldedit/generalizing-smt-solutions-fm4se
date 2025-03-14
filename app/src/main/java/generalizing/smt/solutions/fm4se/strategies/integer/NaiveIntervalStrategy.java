package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.*;

/**
 * NaiveIntervalStrategy finds variable bounds using linear expansion.
 * This strategy:
 * 1. Starts from a known valid value
 * 2. Expands linearly in both directions
 * 3. Stops when unsatisfiable values are found
 * 
 * Performance characteristics:
 * - Time complexity: O(n * r) where n is variables and r is range size
 * - Space complexity: O(1) as it only stores current bounds
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public class NaiveIntervalStrategy extends AbstractIntervalStrategy {

    /**
     * Creates a strategy that analyzes all integer variables.
     *
     * @param connector the SMTConnector providing the Z3 context
     */
    public NaiveIntervalStrategy(SMTConnector connector) {
        super(connector);
    }

    /**
     * Creates a strategy that analyzes a specific variable.
     *
     * @param connector the SMTConnector providing the Z3 context
     * @param variableName name of the variable to analyze
     */
    public NaiveIntervalStrategy(SMTConnector connector, String variableName) {
        super(connector, variableName);
    }

    @Override
    protected String getStrategyName() {
        return "Naive Interval Strategy";
    }

    @Override
    protected Interval findInterval(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int lowerBound = findLowerBound(variable, formula, ctx, startValue);
        int upperBound = findUpperBound(variable, formula, ctx, startValue);
        return new Interval(lowerBound, upperBound);
    }

    /**
     * Finds the lowest possible value using linear expansion.
     */
    private int findLowerBound(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int lowerBound = startValue;
        Solver solver = ctx.mkSolver();
        solver.add(formula);

        while (solver.check() == Status.SATISFIABLE) {
            lowerBound--;
            solver.push();
            solver.add(ctx.mkEq(variable, ctx.mkInt(lowerBound)));
            if (solver.check() != Status.SATISFIABLE) {
                solver.pop();
                lowerBound++;
                break;
            }
            solver.pop();
        }
        return lowerBound;
    }

    /**
     * Finds the highest possible value using linear expansion.
     */
    private int findUpperBound(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int upperBound = startValue;
        Solver solver = ctx.mkSolver();
        solver.add(formula);

        while (solver.check() == Status.SATISFIABLE) {
            upperBound++;
            solver.push();
            solver.add(ctx.mkEq(variable, ctx.mkInt(upperBound)));
            if (solver.check() != Status.SATISFIABLE) {
                solver.pop();
                upperBound--;
                break;
            }
            solver.pop();
        }
        return upperBound;
    }
} 