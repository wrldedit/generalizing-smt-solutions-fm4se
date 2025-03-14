package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import java.util.*;

/**
 * BooleanFormulaStrategy identifies relationships and constraints between Boolean variables.
 */
public class BooleanFormulaStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;

    /**
     * Constructs the Boolean strategy with the given SMTConnector.
     *
     * @param connector the SMTConnector providing the Z3 context.
     */
    public BooleanFormulaStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Boolean Insights:\n");

        // Extract Boolean variables
        List<BoolExpr> variables = extractBooleanVariables(originalFormula, ctx);
        if (variables.isEmpty()) {
            return new GeneralizationResult("Boolean Formula Strategy", "No Boolean variables found.");
        }

        for (BoolExpr var : variables) {
            result.append("\nAnalyzing: ").append(var).append("\n");

            // Always True / Always False
            if (isAlwaysTrue(var, originalFormula, ctx)) {
                result.append("   - Always True\n");
            } else if (isAlwaysFalse(var, originalFormula, ctx)) {
                result.append("   - Always False\n");
            }

            // Conditional Necessity
            if (!connector.isSatisfiable(ctx.mkAnd(originalFormula, ctx.mkNot(var)))) {
                result.append("   - Must be true in all solutions\n");
            }

            // Analyze Pairwise Relationships
            for (BoolExpr otherVar : variables) {
                if (!var.equals(otherVar)) {
                    if (isImplication(var, otherVar, originalFormula, ctx)) {
                        result.append("   - ").append(var).append(" -> ").append(otherVar).append("\n");
                    }
                    if (isMutuallyExclusive(var, otherVar, originalFormula, ctx)) {
                        result.append("   - ").append(var).append(" XOR ").append(otherVar).append("\n");
                    }
                    if (isEquivalent(var, otherVar, originalFormula, ctx)) {
                        result.append("   - ").append(var).append(" <-> ").append(otherVar).append("\n");
                    }
                }
            }
        }

        return new GeneralizationResult("Boolean Formula Strategy", result.toString());
    }

    /**
     * Extracts all Boolean variables appearing in the formula.
     */
    private List<BoolExpr> extractBooleanVariables(BoolExpr formula, Context ctx) {
        List<BoolExpr> variables = new ArrayList<>();
        for (Expr arg : formula.getArgs()) {
            if (arg.isBool() && !variables.contains(arg)) {
                variables.add((BoolExpr) arg);
            }
        }
        return variables;
    }

    /**
     * Checks if a variable is always true.
     */
    private boolean isAlwaysTrue(BoolExpr var, BoolExpr formula, Context ctx) {
        return !connector.isSatisfiable(ctx.mkAnd(formula, ctx.mkNot(var)));
    }

    /**
     * Checks if a variable is always false.
     */
    private boolean isAlwaysFalse(BoolExpr var, BoolExpr formula, Context ctx) {
        return !connector.isSatisfiable(ctx.mkAnd(formula, var));
    }

    /**
     * Checks if `A -> B` always holds.
     */
    private boolean isImplication(BoolExpr a, BoolExpr b, BoolExpr formula, Context ctx) {
        return !connector.isSatisfiable(ctx.mkAnd(formula, a, ctx.mkNot(b)));
    }

    /**
     * Checks if two Boolean variables are mutually exclusive (`A XOR B`).
     */
    private boolean isMutuallyExclusive(BoolExpr a, BoolExpr b, BoolExpr formula, Context ctx) {
        return !connector.isSatisfiable(ctx.mkAnd(formula, a, b));
    }

    /**
     * Checks if two Boolean variables are always equivalent (`A <-> B`).
     */
    private boolean isEquivalent(BoolExpr a, BoolExpr b, BoolExpr formula, Context ctx) {
        return !connector.isSatisfiable(ctx.mkAnd(formula, ctx.mkXor(a, b)));
    }
}