package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import java.util.*;

/**
 * Formula-based strategy that analyzes boolean formulas by examining their structure.
 * The analysis follows these steps:
 * 1. Check each variable to find fixed values (must be true/false)
 * 2. Check each pair of variables to find implications
 */
public class FormulaBasedBooleanStrategy implements BooleanStrategy {
    private final SMTConnector connector;

    public FormulaBasedBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public String analyzeRelations(BoolExpr formula, Set<String> variables) {
        StringBuilder result = new StringBuilder();
        
        // Step 1: Display header
        result.append("Formula-Based Analysis:\n");
        appendVariableList(result, variables);
        
        // Step 2: Find fixed values
        Map<String, Boolean> fixedValues = findFixedValues(formula, variables);
        
        // Step 3: Find implications
        Map<String, List<String>> trueImplications = new HashMap<>();
        Map<String, List<String>> falseImplications = new HashMap<>();
        findImplications(formula, variables, trueImplications, falseImplications);
        
        // Step 4: Display results
        result.append("\nAnalysis Results:\n");
        appendFixedValues(result, fixedValues);
        appendImplications(result, trueImplications, falseImplications);
        
        return result.toString();
    }

    private void appendVariableList(StringBuilder result, Set<String> variables) {
        // Sort variables for consistent output
        List<String> sortedVars = new ArrayList<>(variables);
        Collections.sort(sortedVars);
        
        result.append("Analyzing variables: [");
        result.append(String.join(", ", sortedVars));
        result.append("]\n\n");
    }

    private Map<String, Boolean> findFixedValues(BoolExpr formula, Set<String> variables) {
        Map<String, Boolean> fixedValues = new HashMap<>();
        Context ctx = connector.getContext();
        
        // Check each variable
        for (String var : variables) {
            BoolExpr varExpr = ctx.mkBoolConst(var);
            
            // Check if var must be true
            // This happens when (formula AND NOT var) is unsatisfiable
            if (isUnsatisfiable(ctx.mkAnd(formula, ctx.mkNot(varExpr)))) {
                fixedValues.put(var, true);
                continue;
            }
            
            // Check if var must be false
            // This happens when (formula AND var) is unsatisfiable
            if (isUnsatisfiable(ctx.mkAnd(formula, varExpr))) {
                fixedValues.put(var, false);
            }
        }
        
        return fixedValues;
    }

    private void findImplications(BoolExpr formula, 
                                Set<String> variables,
                                Map<String, List<String>> trueImplications,
                                Map<String, List<String>> falseImplications) {
        Context ctx = connector.getContext();
        
        // Check each pair of variables
        for (String var1 : variables) {
            for (String var2 : variables) {
                // Don't check a variable against itself
                if (var1.equals(var2)) {
                    continue;
                }
                
                BoolExpr expr1 = ctx.mkBoolConst(var1);
                BoolExpr expr2 = ctx.mkBoolConst(var2);
                
                // Check if var1 true implies var2 true
                // This happens when (formula AND var1 AND NOT var2) is unsatisfiable
                if (isUnsatisfiable(ctx.mkAnd(formula, expr1, ctx.mkNot(expr2)))) {
                    // Add to true implications
                    if (!trueImplications.containsKey(var1)) {
                        trueImplications.put(var1, new ArrayList<>());
                    }
                    trueImplications.get(var1).add(var2);
                }
                
                // Check if var1 true implies var2 false
                // This happens when (formula AND var1 AND var2) is unsatisfiable
                else if (isUnsatisfiable(ctx.mkAnd(formula, expr1, expr2))) {
                    // Add to false implications
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

    private boolean isUnsatisfiable(BoolExpr expr) {
        Solver solver = connector.getContext().mkSolver();
        solver.add(expr);
        return solver.check() == Status.UNSATISFIABLE;
    }
} 