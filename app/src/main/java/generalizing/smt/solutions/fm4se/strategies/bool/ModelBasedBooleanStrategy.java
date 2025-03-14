package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import java.util.*;

/**
 * Simple model-based strategy that checks for:
 * 1. Fixed values (variables that are always true/false)
 * 2. Implications between variables using a finite set of models
 */
public class ModelBasedBooleanStrategy implements BooleanStrategy {
    private final SMTConnector connector;
    private static final int MAX_SOLUTIONS = 10;

    public ModelBasedBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    public String analyzeRelations(BoolExpr formula, Set<String> variables) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder();

        // Collect models
        List<Map<String, Boolean>> solutions = collectSolutions(formula, variables, ctx);
        if (solutions.isEmpty()) {
            return "No solutions found.";
        }

        // Part 1: Check for fixed values (single variable properties)
        result.append("Fixed Values:\n");
        boolean foundFixed = false;
        for (String var : variables) {
            Boolean firstValue = solutions.get(0).get(var);
            boolean isFixed = true;
            
            for (int i = 1; i < solutions.size(); i++) {
                if (!solutions.get(i).get(var).equals(firstValue)) {
                    isFixed = false;
                    break;
                }
            }
            
            if (isFixed) {
                foundFixed = true;
                result.append("  ").append(var).append(" is always ").append(firstValue ? "true" : "false").append("\n");
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
                
                boolean impliesTrue = true;  // var1 true -> var2 true
                boolean impliesFalse = true; // var1 true -> var2 false
                
                for (Map<String, Boolean> solution : solutions) {
                    if (solution.get(var1)) { // Only check when var1 is true
                        Boolean val2 = solution.get(var2);
                        if (!val2) impliesTrue = false;
                        if (val2) impliesFalse = false;
                        
                        if (!impliesTrue && !impliesFalse) break;
                    }
                }
                
                if (impliesTrue) {
                    foundImplication = true;
                    result.append("  ").append(var1).append(" = true implies ").append(var2).append(" = true\n");
                } else if (impliesFalse) {
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

    private List<Map<String, Boolean>> collectSolutions(BoolExpr formula, Set<String> variables, Context ctx) {
        List<Map<String, Boolean>> solutions = new ArrayList<>();
        Solver solver = ctx.mkSolver();
        solver.add(formula);
        
        int count = 0;
        while (solver.check() == Status.SATISFIABLE && count < MAX_SOLUTIONS) {
            Model model = solver.getModel();
            Map<String, Boolean> solution = new HashMap<>();
            
            // Extract variable assignments
            for (String var : variables) {
                BoolExpr varExpr = ctx.mkBoolConst(var);
                Expr value = model.evaluate(varExpr, true);
                solution.put(var, value.equals(ctx.mkTrue()));
            }
            
            solutions.add(solution);
            
            // Block this solution
            BoolExpr[] blockingTerms = solution.entrySet().stream()
                .map(entry -> {
                    BoolExpr varExpr = ctx.mkBoolConst(entry.getKey());
                    return entry.getValue() ? ctx.mkNot(varExpr) : varExpr;
                })
                .toArray(BoolExpr[]::new);
            
            solver.add(ctx.mkOr(blockingTerms));
            count++;
        }
        
        return solutions;
    }
} 