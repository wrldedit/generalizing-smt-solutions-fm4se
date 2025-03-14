package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import java.util.*;

public class ModelBasedBooleanStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;
    private final int maxSolutions;

    public ModelBasedBooleanStrategy(SMTConnector connector, int maxSolutions) {
        this.connector = connector;
        this.maxSolutions = maxSolutions;
    }

    @Override
    public GeneralizationResult apply(BoolExpr formula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Model-Based Analysis:\n\n");

        // Collect solutions
        List<Map<String, Boolean>> solutions = collectSolutions(formula, ctx);
        result.append("Found ").append(solutions.size()).append(" solutions:\n");
        
        // Display solutions
        for (Map<String, Boolean> solution : solutions) {
            result.append("  {");
            List<String> assignments = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : solution.entrySet()) {
                assignments.add(entry.getKey() + "=" + entry.getValue());
            }
            result.append(String.join(", ", assignments)).append("}\n");
        }

        // Find implications
        result.append("\nImplications:\n");
        List<String> implications = findImplications(solutions);
        for (String implication : implications) {
            result.append("  ").append(implication).append("\n");
        }

        return new GeneralizationResult("Model-Based Strategy", result.toString());
    }

    private List<Map<String, Boolean>> collectSolutions(BoolExpr formula, Context ctx) {
        List<Map<String, Boolean>> solutions = new ArrayList<>();
        Solver solver = ctx.mkSolver();
        solver.add(formula);
        
        int count = 0;
        while (solver.check() == Status.SATISFIABLE && count < maxSolutions) {
            Model model = solver.getModel();
            Map<String, Boolean> solution = new HashMap<>();
            
            // Extract variable assignments
            for (FuncDecl decl : model.getDecls()) {
                if (decl.getRange().equals(ctx.mkBoolSort())) {
                    String varName = decl.getName().toString();
                    Expr value = model.getConstInterp(decl);
                    if (value != null) {
                        solution.put(varName, value.equals(ctx.mkTrue()));
                    }
                }
            }
            
            // Only add solution if it has all variables
            if (solution.size() == model.getDecls().length) {
                solutions.add(solution);
            }
            
            // Create blocking clause
            BoolExpr blockingClause = createBlockingClause(solution, ctx);
            solver.add(blockingClause);
            
            count++;
        }
        
        return solutions;
    }

    private BoolExpr createBlockingClause(Map<String, Boolean> solution, Context ctx) {
        List<BoolExpr> terms = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : solution.entrySet()) {
            BoolExpr var = ctx.mkBoolConst(entry.getKey());
            terms.add(entry.getValue() ? ctx.mkNot(var) : var);
        }
        return ctx.mkOr(terms.toArray(new BoolExpr[0]));
    }

    private List<String> findImplications(List<Map<String, Boolean>> solutions) {
        if (solutions.isEmpty()) return Collections.emptyList();
        
        List<String> implications = new ArrayList<>();
        Map<String, Boolean> firstSolution = solutions.get(0);
        Set<String> variables = firstSolution.keySet();
        
        for (String var1 : variables) {
            for (String var2 : variables) {
                if (var1.equals(var2)) continue;
                
                boolean isImplication = true;
                for (Map<String, Boolean> solution : solutions) {
                    Boolean val1 = solution.get(var1);
                    Boolean val2 = solution.get(var2);
                    
                    // Skip if either value is null
                    if (val1 == null || val2 == null) {
                        isImplication = false;
                        break;
                    }
                    
                    // Check if var1 → var2 holds in this solution
                    if (val1 && !val2) {
                        isImplication = false;
                        break;
                    }
                }
                
                if (isImplication) {
                    implications.add(var1 + " → " + var2);
                }
            }
        }
        
        return implications;
    }
} 