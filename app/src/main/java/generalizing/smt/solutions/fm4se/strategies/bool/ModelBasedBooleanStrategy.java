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
        
        result.append("Model-Based Analysis:\n");
        result.append("Testing up to " + MAX_SOLUTIONS + " models...\n");

        // Collect models
        List<Map<String, Boolean>> solutions = collectSolutions(formula, variables, ctx);
        if (solutions.isEmpty()) {
            return result.append("No solutions found.").toString();
        }

        // Display models in compact format
        result.append("Found ").append(solutions.size()).append(" satisfying models:\n");
        for (int i = 0; i < solutions.size(); i++) {
            Map<String, Boolean> solution = solutions.get(i);
            result.append(String.format("[%d] {", i + 1));
            boolean first = true;
            List<String> sortedVars = new ArrayList<>(variables);
            Collections.sort(sortedVars);
            for (String var : sortedVars) {
                if (!first) result.append(", ");
                result.append(String.format("%s: %-5s", var, solution.get(var)));
                first = false;
            }
            result.append("}\n");
        }
        result.append("\n");

        // Part 1: Check for fixed values
        result.append("Analysis Results:\n");
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

        // Part 2: Check for implications
        result.append("\nImplications:\n");
        boolean foundImplication = false;
        
        // Map from variable to its implications (both true and false)
        Map<String, List<String>> trueImplications = new HashMap<>();
        Map<String, List<String>> falseImplications = new HashMap<>();
        
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
                    if (!trueImplications.containsKey(var1)) {
                        trueImplications.put(var1, new ArrayList<>());
                    }
                    trueImplications.get(var1).add(var2);
                } else if (impliesFalse) {
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
            List<String> sortedVars = new ArrayList<>(variables);
            Collections.sort(sortedVars);
            
            for (String var : sortedVars) {
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