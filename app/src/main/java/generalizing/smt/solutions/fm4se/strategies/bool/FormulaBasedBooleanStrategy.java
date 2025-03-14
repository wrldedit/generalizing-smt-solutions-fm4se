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

        result.append("Formula-Based Analysis:\n");
        result.append("Testing all variables [");
        List<String> sortedVars = new ArrayList<>(variables);
        Collections.sort(sortedVars);
        result.append(String.join(", ", sortedVars));
        result.append("] for:\n");
        result.append("- Fixed values (always true/false)\n");
        result.append("- Direct implications (x -> y)\n");
        result.append("- Bidirectional implications (x <-> y)\n\n");

        result.append("Analysis Results:\n");
        // Part 1: Check for fixed values
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

        // Part 2: Check for implications
        result.append("\nImplications:\n");
        boolean foundImplication = false;
        
        // Map from variable to its implications (both true and false)
        Map<String, List<String>> trueImplications = new HashMap<>();
        Map<String, List<String>> falseImplications = new HashMap<>();
        
        for (String var1 : variables) {
            for (String var2 : variables) {
                if (var1.equals(var2)) continue;
                
                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);
                
                // Check if v1 true implies v2 true
                // (formula ∧ v1 ∧ ¬v2 is unsatisfiable)
                if (isUnsatisfiable(ctx.mkAnd(formula, v1, ctx.mkNot(v2)))) {
                    foundImplication = true;
                    if (!trueImplications.containsKey(var1)) {
                        trueImplications.put(var1, new ArrayList<>());
                    }
                    trueImplications.get(var1).add(var2);
                }
                
                // Check if v1 true implies v2 false
                // (formula ∧ v1 ∧ v2 is unsatisfiable)
                if (isUnsatisfiable(ctx.mkAnd(formula, v1, v2))) {
                    foundImplication = true;
                    if (!falseImplications.containsKey(var1)) {
                        falseImplications.put(var1, new ArrayList<>());
                    }
                    falseImplications.get(var1).add(var2);
                }
            }
        }

        // Output consolidated implications
        if (foundImplication) {
            List<String> varsForImplications = new ArrayList<>(variables);
            Collections.sort(varsForImplications);
            
            for (String var : varsForImplications) {
                List<String> trueList = trueImplications.get(var);
                List<String> falseList = falseImplications.get(var);
                
                if (trueList != null && !trueList.isEmpty()) {
                    Collections.sort(trueList);
                    result.append("  ").append(var).append(" = true implies ");
                    if (trueList.size() == 1) {
                        result.append(trueList.get(0)).append(" = true\n");
                    } else {
                        result.append("all of {");
                        result.append(String.join(", ", trueList));
                        result.append("} = true\n");
                    }
                }
                
                if (falseList != null && !falseList.isEmpty()) {
                    Collections.sort(falseList);
                    result.append("  ").append(var).append(" = true implies ");
                    if (falseList.size() == 1) {
                        result.append(falseList.get(0)).append(" = false\n");
                    } else {
                        result.append("all of {");
                        result.append(String.join(", ", falseList));
                        result.append("} = false\n");
                    }
                }
            }
        } else {
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