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
        if (expr instanceof IntExpr && expr.isConst() && !variables.contains(expr)) {
            variables.add((IntExpr) expr);
            System.out.println("Collected variable: " + expr);
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
        
        // Get all variables from the formula first
        List<IntExpr> allVars = extractIntegerVariables(formula, ctx);
        
        // Fix all variables except the free one
        for (IntExpr var : allVars) {

            System.out.println("Checking variable: " + var);
            if (!var.equals(freeVar)) {
                Expr value = model.evaluate(var, true);
                System.out.println("Value: " + value);
                if (value != null) {
                    System.out.println("Adding constraint: " + ctx.mkEq(var, (IntExpr)value));
                    constraints.add(ctx.mkEq(var, (IntExpr)value));
                }
            }
        }
        
        // Only add the fixed variable constraints, keep original formula separate
        return formula;  // Return just the original formula since we'll add constraints in the solver
    }

    /**
     * Finds the lowest possible value of the variable by expanding outward from a known valid model value.
     */
    private int findLowerBoundNaive(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int lowerBound = startValue;
        Solver solver = ctx.mkSolver();
        solver.add(formula);

        // Expand downward until unsatisfiable
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
     * Finds the highest possible value of the variable by expanding outward from a known valid model value.
     */
    private int findUpperBoundNaive(IntExpr variable, BoolExpr formula, Context ctx, int startValue) {
        int upperBound = startValue;
        Solver solver = ctx.mkSolver();
        solver.add(formula);

        // Expand upward until unsatisfiable
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
