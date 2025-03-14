package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.*;
import java.util.*;

/**
 * NaiveIntegerStrategy finds variable bounds using linear expansion.
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
public class NaiveIntegerStrategy implements IntegerStrategy {
    private final SMTConnector connector;

    public NaiveIntegerStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public String analyzeIntegers(BoolExpr formula, Set<String> variables) {
        StringBuilder result = new StringBuilder();
        result.append("Naive Integer Strategy (Linear Search):\n");
        result.append("Assumptions:\n");
        result.append("- Variables have finite bounds within [-1000, 1000]\n");
        result.append("- Linear search from center outwards\n\n");
        
        result.append("Found variable ranges:\n");
        
        // Analyze each variable independently
        for (String varName : variables) {
            int min = findMinValue(formula, varName);
            int max = findMaxValue(formula, varName);
            result.append(String.format("%s: [%d, %d]\n", varName, min, max));
        }
        
        return result.toString();
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

    private int findMinValue(BoolExpr formula, String varName) {
        Context ctx = connector.getContext();
        IntExpr var = ctx.mkIntConst(varName);
        int currentValue = -1000; // Start with a reasonable lower bound
        
        while (true) {
            BoolExpr test = ctx.mkAnd(formula, ctx.mkEq(var, ctx.mkInt(currentValue)));
            if (connector.isSatisfiable(test)) {
                return currentValue;
            }
            currentValue++;
            if (currentValue > 1000) { // Safety check
                return currentValue - 1;
            }
        }
    }

    private int findMaxValue(BoolExpr formula, String varName) {
        Context ctx = connector.getContext();
        IntExpr var = ctx.mkIntConst(varName);
        int currentValue = 1000; // Start with a reasonable upper bound
        
        while (true) {
            BoolExpr test = ctx.mkAnd(formula, ctx.mkEq(var, ctx.mkInt(currentValue)));
            if (connector.isSatisfiable(test)) {
                return currentValue;
            }
            currentValue--;
            if (currentValue < -1000) { // Safety check
                return currentValue + 1;
            }
        }
    }
} 