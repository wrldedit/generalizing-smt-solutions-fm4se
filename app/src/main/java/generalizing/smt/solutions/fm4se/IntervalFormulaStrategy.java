package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;


import java.util.ArrayList;
import java.util.List;

/**
 * IntervalFormulaStrategy determines the valid range of values
 * for variables by fixing all but one variable and expanding outward until unsatisfiable.
 */
public class IntervalFormulaStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;
    private final String targetVariable; // New: Allow specific variable selection

    /**
     * Constructs the strategy for all variables.
     *
     * @param connector the SMTConnector providing the Z3 context.
     */
    public IntervalFormulaStrategy(SMTConnector connector) {
        this.connector = connector;
        this.targetVariable = null; // Compute for all variables
    }

    /**
     * Constructs the strategy for a specific variable.
     *
     * @param connector the SMTConnector providing the Z3 context.
     * @param variableName the name of the variable to analyze.
     */
    public IntervalFormulaStrategy(SMTConnector connector, String variableName) {
        this.connector = connector;
        this.targetVariable = variableName; // Compute only for a specific variable
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Determined interval(s):\n");

        // Extract integer variables
        List<IntExpr> variables = extractIntegerVariables(originalFormula, ctx);
        if (variables.isEmpty()) {
            return new GeneralizationResult("Interval Formula Strategy", "No integer variables found in the formula.");
        }

        // If a target variable is specified, filter the list
        if (targetVariable != null) {
            variables.removeIf(var -> !var.toString().equals(targetVariable));
            if (variables.isEmpty()) {
                return new GeneralizationResult("Interval Formula Strategy", "Variable '" + targetVariable + "' not found.");
            }
        }

        // Get a model to use for fixing values
        Model model = connector.getSolution(originalFormula);
        if (model == null) {
            return new GeneralizationResult("Interval Formula Strategy", "No solution found for the formula.");
        }

        for (IntExpr variable : variables) {
            // Fix all other variables using a valid model
            BoolExpr formulaWithFixedValues = fixAllExcept(originalFormula, variable, ctx, model);

            // Get the starting value from the model
            Expr value = model.evaluate(variable, true);
            if (value == null) {
                continue;
            }
            int startValue = ((IntNum)value).getInt();

            // Find interval bounds using optimized naive expansion
            int lowerBound = findLowerBoundNaive(variable, formulaWithFixedValues, ctx, startValue);
            int upperBound = findUpperBoundNaive(variable, formulaWithFixedValues, ctx, startValue);

            result.append(variable + " ∈ [" + lowerBound + ", " + upperBound + "]\n");

            // Find tighter bounds using binary search (optional)
            int lowerBoundBS = findLowerBoundBinary(variable, formulaWithFixedValues, ctx, lowerBound - 50, startValue);
            int upperBoundBS = findUpperBoundBinary(variable, formulaWithFixedValues, ctx, startValue, upperBound + 50);

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
     * Finds the lowest possible value of the variable by expanding outward from a known valid model value.
     */
    private int findLowerBoundNaive(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int lowerBound = startValue;

        // Expand downward until unsatisfiable
        while (connector.isSatisfiable(ctx.mkAnd(formula, ctx.mkEq(variable, ctx.mkInt(lowerBound))))) {
            lowerBound--;
        }
        return lowerBound + 1; // The last satisfiable value
    }

    /**
     * Finds the highest possible value of the variable by expanding outward from a known valid model value.
     */
    private int findUpperBoundNaive(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int upperBound = startValue;

        // Expand upward until unsatisfiable
        while (connector.isSatisfiable(ctx.mkAnd(formula, ctx.mkEq(variable, ctx.mkInt(upperBound))))) {
            upperBound++;
        }
        return upperBound - 1; // The last satisfiable value
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
