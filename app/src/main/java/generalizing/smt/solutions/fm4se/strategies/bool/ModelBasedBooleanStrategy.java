package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import java.util.*;

/**
 * Model-based strategy that analyzes boolean formulas by examining solutions.
 * The analysis follows these steps:
 * 1. Generate all possible models (up to MAX_SOLUTIONS)
 * 2. Analyze models to find fixed values
 * 3. Analyze models to find implications between variables
 */
public class ModelBasedBooleanStrategy implements BooleanStrategy {
    private final SMTConnector connector;
    private static final int MAX_SOLUTIONS = 10;

    public ModelBasedBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public String analyzeRelations(BoolExpr formula, Set<String> variables) {
        StringBuilder result = new StringBuilder();
        
        // Step 1: Generate models
        List<Map<String, Boolean>> models = generateModels(formula, variables);
        if (models.isEmpty()) {
            return "No solutions found.";
        }
        
        // Step 2: Display results
        result.append("Model-Based Analysis:\n");
        result.append("Testing up to " + MAX_SOLUTIONS + " models...\n\n");
        appendModels(result, models, variables);
        
        // Step 3: Analyze patterns
        result.append("\nAnalysis Results:\n");
        
        // Find and display fixed values
        Map<String, Boolean> fixedValues = findFixedValues(models, variables);
        appendFixedValues(result, fixedValues);
        
        // Find and display implications
        Map<String, List<String>> trueImplications = new HashMap<>();
        Map<String, List<String>> falseImplications = new HashMap<>();
        findImplications(models, variables, trueImplications, falseImplications);
        appendImplications(result, trueImplications, falseImplications);
        
        return result.toString();
    }

    private List<Map<String, Boolean>> generateModels(BoolExpr formula, Set<String> variables) {
        List<Map<String, Boolean>> models = new ArrayList<>();
        Context ctx = connector.getContext();
        Solver solver = ctx.mkSolver();
        solver.add(formula);
        
        int count = 0;
        while (solver.check() == Status.SATISFIABLE && count < MAX_SOLUTIONS) {
            // Extract current model
            Map<String, Boolean> currentModel = extractValueAssignments(solver.getModel(), variables);
            models.add(currentModel);
            
            // Block current model to find a different one
            solver.add(buildBlockingConstraint(currentModel));
            count++;
        }
        
        return models;
    }

    private Map<String, Boolean> extractValueAssignments(Model model, Set<String> variables) {
        Map<String, Boolean> assignments = new HashMap<>();
        Context ctx = connector.getContext();
        
        for (String var : variables) {
            BoolExpr varExpr = ctx.mkBoolConst(var);
            Expr value = model.evaluate(varExpr, true);
            assignments.put(var, value.equals(ctx.mkTrue()));
        }
        
        return assignments;
    }

    private BoolExpr buildBlockingConstraint(Map<String, Boolean> model) {
        Context ctx = connector.getContext();
        List<BoolExpr> differentValues = new ArrayList<>();
        
        for (String varName : model.keySet()) {
            boolean currentValue = model.get(varName);
            BoolExpr var = ctx.mkBoolConst(varName);
            
            // Add constraint for opposite value:
            // If current value is true, add (var = false)
            // If current value is false, add (var = true)
            if (currentValue == true) {
                differentValues.add(ctx.mkNot(var));  // Force var to be false
            } else {
                differentValues.add(var);             // Force var to be true
            }
        }
        
        // At least one variable must be different
        return ctx.mkOr(differentValues.toArray(new BoolExpr[0]));
    }

    private void appendModels(StringBuilder result, 
                            List<Map<String, Boolean>> models, 
                            Set<String> variables) {
        result.append("Found ").append(models.size()).append(" satisfying models:\n");
        
        // Sort variables for consistent output
        List<String> sortedVars = new ArrayList<>(variables);
        Collections.sort(sortedVars);
        
        // Display each model
        for (int i = 0; i < models.size(); i++) {
            Map<String, Boolean> model = models.get(i);
            result.append("[").append(i + 1).append("] {");
            
            boolean first = true;
            for (String var : sortedVars) {
                if (!first) {
                    result.append(", ");
                }
                result.append(var).append(": ");
                result.append(model.get(var));
                first = false;
            }
            result.append("}\n");
        }
        result.append("\n");
    }

    private Map<String, Boolean> findFixedValues(List<Map<String, Boolean>> models, 
                                               Set<String> variables) {
        Map<String, Boolean> fixedValues = new HashMap<>();
        if (models.isEmpty()) {
            return fixedValues;
        }

        // Check each variable
        for (String var : variables) {
            // Get the value in the first model
            boolean firstValue = models.get(0).get(var);
            boolean isFixed = true;
            
            // Check if the value changes in any other model
            for (int i = 1; i < models.size(); i++) {
                boolean currentValue = models.get(i).get(var);
                if (currentValue != firstValue) {
                    isFixed = false;
                    break;
                }
            }
            
            // If the value never changed, it's fixed
            if (isFixed) {
                fixedValues.put(var, firstValue);
            }
        }
        
        return fixedValues;
    }

    private void findImplications(List<Map<String, Boolean>> models, 
                                Set<String> variables,
                                Map<String, List<String>> trueImplications,
                                Map<String, List<String>> falseImplications) {
        if (models.isEmpty()) {
            return;
        }

        // Check each pair of variables
        for (String var1 : variables) {
            for (String var2 : variables) {
                // Don't check a variable against itself
                if (var1.equals(var2)) {
                    continue;
                }
                
                boolean impliesTrue = true;
                boolean impliesFalse = true;
                
                // Check each model
                for (Map<String, Boolean> model : models) {
                    boolean value1 = model.get(var1);
                    boolean value2 = model.get(var2);
                    
                    // Only check implications when var1 is true
                    if (value1) {
                        // If we find a counterexample, the implication doesn't hold
                        if (!value2) {
                            impliesTrue = false;
                        }
                        if (value2) {
                            impliesFalse = false;
                        }
                        
                        // If neither implication holds, we can stop checking
                        if (!impliesTrue && !impliesFalse) {
                            break;
                        }
                    }
                }
                
                // Add true implication if found
                if (impliesTrue) {
                    if (!trueImplications.containsKey(var1)) {
                        trueImplications.put(var1, new ArrayList<>());
                    }
                    trueImplications.get(var1).add(var2);
                }
                
                // Add false implication if found
                if (impliesFalse) {
                    if (!falseImplications.containsKey(var1)) {
                        falseImplications.put(var1, new ArrayList<>());
                    }
                    falseImplications.get(var1).add(var2);
                }
            }
        }
    }

    private void appendFixedValues(StringBuilder result, Map<String, Boolean> fixedValues) {
        result.append("Fixed Values:\n");
        if (fixedValues.isEmpty()) {
            result.append("  None found\n");
            return;
        }

        // Sort variables for consistent output
        List<String> sortedVars = new ArrayList<>(fixedValues.keySet());
        Collections.sort(sortedVars);
        
        // Display each fixed value
        for (String var : sortedVars) {
            result.append("  ");
            result.append(var);
            result.append(" is always ");
            if (fixedValues.get(var)) {
                result.append("true");
            } else {
                result.append("false");
            }
            result.append("\n");
        }
    }

    private void appendImplications(StringBuilder result,
                                  Map<String, List<String>> trueImplications,
                                  Map<String, List<String>> falseImplications) {
        result.append("\nImplications:\n");
        if (trueImplications.isEmpty() && falseImplications.isEmpty()) {
            result.append("  None found\n");
            return;
        }

        // Get all source variables and sort them
        Set<String> allSources = new HashSet<>();
        allSources.addAll(trueImplications.keySet());
        allSources.addAll(falseImplications.keySet());
        List<String> sortedSources = new ArrayList<>(allSources);
        Collections.sort(sortedSources);
        
        // Display implications for each source variable
        for (String source : sortedSources) {
            // Display true implications
            if (trueImplications.containsKey(source)) {
                appendImplicationList(result, source, trueImplications.get(source), true);
            }
            // Display false implications
            if (falseImplications.containsKey(source)) {
                appendImplicationList(result, source, falseImplications.get(source), false);
            }
        }
    }

    private void appendImplicationList(StringBuilder result, String var, 
                                    List<String> implications, boolean impliedValue) {
        if (implications.isEmpty()) {
            return;
        }
        
        // Sort implications for consistent output
        Collections.sort(implications);
        
        // Start the implication line
        result.append("  ");
        result.append(var);
        result.append(" = true implies ");
        
        // For single implications
        if (implications.size() == 1) {
            result.append(implications.get(0));
            result.append(" = ");
            if (impliedValue) {
                result.append("true");
            } else {
                result.append("false");
            }
            result.append("\n");
        } 
        // For multiple implications
        else {
            result.append("all of {");
            result.append(String.join(", ", implications));
            result.append("} = ");
            if (impliedValue) {
                result.append("true");
            } else {
                result.append("false");
            }
            result.append("\n");
        }
    }
} 