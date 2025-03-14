package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import java.util.*;

/**
 * Simple formula-based strategy that checks for:
 * 1. Fixed values (variables that are always true/false) by checking formula structure
 * 2. Implications between variables using counterexamples
 */
public class FormulaBasedBooleanStrategy implements BooleanStrategy {
    private final SMTConnector connector;

    public FormulaBasedBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    public String analyzeRelations(BoolExpr formula, Set<String> variables) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder();

        // Part 1: Check for fixed values (single variable properties)
        result.append("Fixed Values:\n");
        boolean foundFixed = false;
        for (String var : variables) {
            BoolExpr varExpr = ctx.mkBoolConst(var);
            
            // Check if var must be true (formula ∧ ¬var is unsatisfiable)
            if (isUnsatisfiable(ctx.mkAnd(formula, ctx.mkNot(varExpr)))) {
                foundFixed = true;
                result.append("  ").append(var).append(" is always true\n");
                continue;
            }
            
            // Check if var must be false (formula ∧ var is unsatisfiable)
            if (isUnsatisfiable(ctx.mkAnd(formula, varExpr))) {
                foundFixed = true;
                result.append("  ").append(var).append(" is always false\n");
            }
        }
        if (!foundFixed) {
            result.append("  None found\n");
        }

        // Part 2: Check for implications (relationships between variables)
        result.append("\nImplications:\n");
        boolean foundImplication = false;
        for (String var1 : variables) {
            for (String var2 : variables) {
                if (var1.equals(var2)) continue;
                
                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);
                
                // Check if v1 true implies v2 true
                // (formula ∧ v1 ∧ ¬v2 is unsatisfiable)
                if (isUnsatisfiable(ctx.mkAnd(formula, v1, ctx.mkNot(v2)))) {
                    foundImplication = true;
                    result.append("  ").append(var1).append(" = true implies ").append(var2).append(" = true\n");
                }
                
                // Check if v1 true implies v2 false
                // (formula ∧ v1 ∧ v2 is unsatisfiable)
                if (isUnsatisfiable(ctx.mkAnd(formula, v1, v2))) {
                    foundImplication = true;
                    result.append("  ").append(var1).append(" = true implies ").append(var2).append(" = false\n");
                }
            }
        }
        if (!foundImplication) {
            result.append("  None found\n");
        }

        return result.toString();
    }

    private boolean isUnsatisfiable(BoolExpr expr) {
        Solver solver = connector.getContext().mkSolver();
        solver.add(expr);
        return solver.check() == Status.UNSATISFIABLE;
    }
} 