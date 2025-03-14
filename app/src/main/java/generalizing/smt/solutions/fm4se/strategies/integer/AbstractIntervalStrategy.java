package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.*;
import java.util.*;

/**
 * AbstractIntervalStrategy provides common functionality for interval-based analysis.
 * This abstract class:
 * 1. Extracts integer variables from formulas
 * 2. Manages variable filtering and model evaluation
 * 3. Provides base structure for interval finding
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public abstract class AbstractIntervalStrategy implements FormulaBasedGeneralizationStrategy {
    protected final SMTConnector connector;
    protected final String targetVariable;

    /**
     * Constructs the strategy for all variables.
     *
     * @param connector the SMTConnector providing the Z3 context.
     */
    protected AbstractIntervalStrategy(SMTConnector connector) {
        this.connector = connector;
        this.targetVariable = null;
    }

    /**
     * Constructs the strategy for a specific variable.
     *
     * @param connector the SMTConnector providing the Z3 context.
     * @param variableName the name of the variable to analyze.
     */
    protected AbstractIntervalStrategy(SMTConnector connector, String variableName) {
        this.connector = connector;
        this.targetVariable = variableName;
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder(getStrategyName() + ":\n");

        // Extract integer variables
        List<IntExpr> variables = extractIntegerVariables(originalFormula, ctx);
        if (variables.isEmpty()) {
            return new GeneralizationResult(getStrategyName(), "No integer variables found in the formula.");
        }

        // If a target variable is specified, filter the list
        if (targetVariable != null) {
            variables.removeIf(var -> !var.toString().equals(targetVariable));
            if (variables.isEmpty()) {
                return new GeneralizationResult(getStrategyName(), "Variable '" + targetVariable + "' not found.");
            }
        }

        // Get a model to use for fixing values
        Model model = connector.getSolution(originalFormula);
        if (model == null) {
            return new GeneralizationResult(getStrategyName(), "No solution found for the formula.");
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

            // Find interval bounds using strategy-specific method
            Interval interval = findInterval(variable, formulaWithFixedValues, ctx, startValue);
            result.append(String.format("%s ∈ [%d, %d]\n", 
                variable, interval.getLowerBound(), interval.getUpperBound()));
        }

        return new GeneralizationResult(getStrategyName(), result.toString());
    }

    /**
     * Gets the name of this strategy implementation.
     */
    protected abstract String getStrategyName();

    /**
     * Finds the interval bounds for a variable using a specific strategy.
     */
    protected abstract Interval findInterval(IntExpr variable, BoolExpr formula, Context ctx, int startValue);

    /**
     * Extracts all integer variables appearing in the formula.
     */
    protected List<IntExpr> extractIntegerVariables(BoolExpr formula, Context ctx) {
        List<IntExpr> variables = new ArrayList<>();
        collectIntegerVariables(formula, variables, ctx);
        return variables;
    }

    /**
     * Recursively collects all integer variables in a formula.
     */
    private void collectIntegerVariables(Expr expr, List<IntExpr> variables, Context ctx) {
        if (expr instanceof IntExpr && expr.isConst() && !variables.contains(expr)) {
            variables.add((IntExpr) expr);
        }
        for (Expr arg : expr.getArgs()) {
            collectIntegerVariables(arg, variables, ctx);
        }
    }

    /**
     * Fixes all variables in the formula except the given one, using values from a model.
     */
    protected BoolExpr fixAllExcept(BoolExpr formula, IntExpr freeVar, Context ctx, Model model) {
        List<BoolExpr> constraints = new ArrayList<>();
        List<IntExpr> allVars = extractIntegerVariables(formula, ctx);
        
        for (IntExpr var : allVars) {
            if (!var.equals(freeVar)) {
                Expr value = model.evaluate(var, true);
                if (value != null) {
                    constraints.add(ctx.mkEq(var, (IntExpr)value));
                }
            }
        }
        
        return ctx.mkAnd(formula, ctx.mkAnd(constraints.toArray(new BoolExpr[0])));
    }

    /**
     * Represents an interval with lower and upper bounds.
     */
    protected static class Interval {
        private final int lowerBound;
        private final int upperBound;

        public Interval(int lowerBound, int upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        public int getLowerBound() {
            return lowerBound;
        }

        public int getUpperBound() {
            return upperBound;
        }
    }
} 