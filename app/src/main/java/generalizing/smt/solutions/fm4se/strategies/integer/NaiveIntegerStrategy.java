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
    private static final int MAX_BOUND = 1000;  // Maximum search boundary
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
        
        // Get initial model to find starting points
        Model model = connector.getSolution(formula);
        Context ctx = connector.getContext();
        
        // Analyze each variable independently
        for (String varName : variables) {
            IntExpr var = ctx.mkIntConst(varName);
            
            // Get starting value from model or use 0 as default
            int startValue = 0;
            if (model != null) {
                Expr value = model.evaluate(var, true);
                if (value instanceof IntNum) {
                    startValue = ((IntNum)value).getInt();
                }
            }
            
            int min = findLowerBound(var, formula, ctx, startValue);
            int max = findUpperBound(var, formula, ctx, startValue);
            result.append(String.format("%s: [%d, %d]\n", varName, min, max));
        }
        
        return result.toString();
    }

    /**
     * Finds the lowest possible value by expanding downward from start value.
     * @param variable The variable to find the bound for
     * @param formula The formula containing constraints
     * @param ctx The Z3 context
     * @param startValue The value to start searching from
     * @return The lowest satisfiable value found
     */
    private int findLowerBound(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        Solver solver = ctx.mkSolver();
        solver.add(formula);
        int currentValue = startValue;
        
        // First check if start value is satisfiable
        solver.push();
        solver.add(ctx.mkEq(variable, ctx.mkInt(currentValue)));
        if (solver.check() != Status.SATISFIABLE) {
            solver.pop();
            return -MAX_BOUND; // Return minimum bound if start value not satisfiable
        }
        solver.pop();
        
        // Search downward until unsatisfiable value is found
        while (currentValue > -MAX_BOUND) {
            currentValue--;
            solver.push();
            solver.add(ctx.mkEq(variable, ctx.mkInt(currentValue)));
            
            if (solver.check() != Status.SATISFIABLE) {
                solver.pop();
                return currentValue + 1; // Return last satisfiable value
            }
            solver.pop();
        }
        
        return -MAX_BOUND; // Return minimum bound if no unsatisfiable value found
    }

    /**
     * Finds the highest possible value by expanding upward from start value.
     * @param variable The variable to find the bound for
     * @param formula The formula containing constraints
     * @param ctx The Z3 context
     * @param startValue The value to start searching from
     * @return The highest satisfiable value found
     */
    private int findUpperBound(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        Solver solver = ctx.mkSolver();
        solver.add(formula);
        int currentValue = startValue;
        
        // First check if start value is satisfiable
        solver.push();
        solver.add(ctx.mkEq(variable, ctx.mkInt(currentValue)));
        if (solver.check() != Status.SATISFIABLE) {
            solver.pop();
            return MAX_BOUND; // Return maximum bound if start value not satisfiable
        }
        solver.pop();
        
        // Search upward until unsatisfiable value is found
        while (currentValue < MAX_BOUND) {
            currentValue++;
            solver.push();
            solver.add(ctx.mkEq(variable, ctx.mkInt(currentValue)));
            
            if (solver.check() != Status.SATISFIABLE) {
                solver.pop();
                return currentValue - 1; // Return last satisfiable value
            }
            solver.pop();
        }
        
        return MAX_BOUND; // Return maximum bound if no unsatisfiable value found
    }
} 