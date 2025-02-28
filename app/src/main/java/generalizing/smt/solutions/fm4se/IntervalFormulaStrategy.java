package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

import java.util.ArrayList;
import java.util.List;

/**
 * IntervalFormulaStrategy automatically determines the valid range of values
 * for each variable by iterating through the formula, fixing all but one variable,
 * and expanding the possible interval using two methods:
 * <ul>
 *     <li>Naive expansion (incrementing/decrementing by 1)</li>
 *     <li>Binary search for efficiency</li>
 * </ul>
 */
public class IntervalFormulaStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;

    /**
     * Constructs the strategy with the given SMTConnector.
     *
     * @param connector the SMTConnector providing the Z3 context.
     */
    public IntervalFormulaStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Automatically determined intervals:\n");
    
        // Extract integer variables
        List<IntExpr> variables = extractIntegerVariables(originalFormula, ctx);
        if (variables.isEmpty()) {
            return new GeneralizationResult("Interval Formula Strategy", "No integer variables found in the formula.");
        }
    
        // Get a model to use for fixing values
        Model model = connector.getSolution(originalFormula);
        if (model == null) {
            return new GeneralizationResult("Interval Formula Strategy", "No solution found for the formula.");
        }
    
        for (IntExpr variable : variables) {
            // Fix all other variables using a valid model
            BoolExpr formulaWithFixedValues = fixAllExcept(originalFormula, variable, ctx, model);
    
/*             // Find interval bounds using naive expansion
            int lowerBound = findLowerBoundNaive(variable, formulaWithFixedValues, ctx);
            int upperBound = findUpperBoundNaive(variable, formulaWithFixedValues, ctx);
    
            result.append(variable + " ∈ [" + lowerBound + ", " + upperBound + "]\n"); */
    
            // Find tighter bounds using binary search
            int lowerBoundBS = findLowerBoundBinary(variable, formulaWithFixedValues, ctx, -1000, 1000);
            int upperBoundBS = findUpperBoundBinary(variable, formulaWithFixedValues, ctx, -1000 , 1000);
    
            result.append("   Binary Search: " + variable + " ∈ [" + lowerBoundBS + ", " + upperBoundBS + "]\n");
        }
    
        return new GeneralizationResult("Interval Formula Strategy", result.toString());
    }

    /**
     * Extracts all integer variables appearing in the formula.
     */
    private List<IntExpr> extractIntegerVariables(BoolExpr formula, Context ctx) {
        List<IntExpr> variables = new ArrayList<>();
        // Recursively traverse the formula tree
        collectIntegerVariables(formula, variables, ctx);
        return variables;
    }

    /**
     * Recursively collects all integer variables in a formula.
     */
    private void collectIntegerVariables(Expr expr, List<IntExpr> variables, Context ctx) {
        if (expr instanceof IntExpr && !variables.contains(expr)) {
            variables.add((IntExpr) expr);
        }
        for (Expr arg : expr.getArgs()) {
            collectIntegerVariables(arg, variables, ctx);
        }
    }

    /**
     * Fixes all variables in the formula except the given one, using values from a model.
     */
    private BoolExpr fixAllExcept(BoolExpr formula, IntExpr freeVar, Context ctx, Model model) {
        List<BoolExpr> constraints = new ArrayList<>();

        for (FuncDecl decl : model.getConstDecls()) {
            if (decl.getRange().equals(ctx.getIntSort()) && !decl.getName().toString().equals(freeVar.toString())) {
                IntExpr var = (IntExpr) ctx.mkConst(decl.getName(), decl.getRange());
                IntNum value = (IntNum) model.getConstInterp(decl); // Get model-assigned value
                if (value != null) {
                    constraints.add(ctx.mkEq(var, value)); // Fix variable
                }
            }
        }
        return ctx.mkAnd(formula, ctx.mkAnd(constraints.toArray(new BoolExpr[0])));
    }

    /**
     * Finds the lowest possible value of the variable using a naive increment approach.
     */
    private int findLowerBoundNaive(IntExpr variable, BoolExpr formula, Context ctx) {
        int bound = -1000; // Start from an arbitrary low value
        while (connector.isSatisfiable(ctx.mkAnd(formula, ctx.mkEq(variable, ctx.mkInt(bound))))) {
            bound--; // Expand downward
        }
        return bound + 1; // The last valid value
    }

    /**
     * Finds the highest possible value of the variable using a naive increment approach.
     */
    private int findUpperBoundNaive(IntExpr variable, BoolExpr formula, Context ctx) {
        int bound = 1000; // Start from an arbitrary high value
        while (connector.isSatisfiable(ctx.mkAnd(formula, ctx.mkEq(variable, ctx.mkInt(bound))))) {
            bound++; // Expand upward
        }
        return bound - 1; // The last valid value
    }

    /**
     * Uses binary search to efficiently find the lowest bound.
     */
    private int findLowerBoundBinary(IntExpr variable, BoolExpr formula, Context ctx, int low, int high) {
        while (low < high) {
            int mid = (low + high) / 2;
            BoolExpr test = ctx.mkAnd(formula, ctx.mkLe(variable, ctx.mkInt(mid)));
            if (connector.isSatisfiable(test)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }
        return low;
    }

    /**
     * Uses binary search to efficiently find the highest bound.
     */
    private int findUpperBoundBinary(IntExpr variable, BoolExpr formula, Context ctx, int low, int high) {
        while (low < high) {
            int mid = (low + high + 1) / 2;
            BoolExpr test = ctx.mkAnd(formula, ctx.mkGe(variable, ctx.mkInt(mid)));
            if (connector.isSatisfiable(test)) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return high;
    }
}
