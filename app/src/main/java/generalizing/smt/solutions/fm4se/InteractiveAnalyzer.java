package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.strategies.bool.*;
import generalizing.smt.solutions.fm4se.strategies.integer.*;
import generalizing.smt.solutions.fm4se.strategies.bool.ModelBasedBooleanStrategy;
import generalizing.smt.solutions.fm4se.strategies.bool.FormulaBasedBooleanStrategy;
import generalizing.smt.solutions.fm4se.strategies.RelationType;
import generalizing.smt.solutions.fm4se.SMTConnector;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * InteractiveAnalyzer provides an interactive session for analyzing formulas.
 * This class:
 * 1. Presents a menu of available analysis strategies
 * 2. Handles user input for strategy selection
 * 3. Executes the selected strategy and displays results
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public class InteractiveAnalyzer {

    private final SMTConnector connector;

    /**
     * Constructs an InteractiveAnalyzer with the given SMTConnector.
     * @param connector the SMTConnector providing the Z3 context and checking methods.
     */
    public InteractiveAnalyzer(SMTConnector connector) {
        this.connector = connector;
    }

    public void runInteractiveSession(BoolExpr formula) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            try {
                System.out.println("\nSelect analysis type:");
                System.out.println("1: Boolean Analysis");
                System.out.println("2: Interval Analysis");
                System.out.println("q: Quit");
                System.out.print("Enter your choice: ");
                
                String choice = scanner.nextLine().trim();

                if (choice.equals("q")) {
                    System.out.println("Exiting interactive session.");
                    running = false;
                    continue;
                }

                switch (choice) {
                    case "1":
                        handleBooleanStrategies(formula, scanner);
                        break;
                    case "2":
                        handleIntervalStrategy(formula, scanner);
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error processing input: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }
    
    private void handleBooleanStrategies(BoolExpr formula, Scanner scanner) {
        System.out.println("\nSelect Analysis Strategy:");
        System.out.println("1. Solution Space Analysis (examines actual solutions)");
        System.out.println("2. Formula Structure Analysis (examines logical structure)");
        System.out.println("3. Combined Analysis (both strategies)");
        System.out.print("Enter your choice (1-3): ");

        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            StringBuilder result = new StringBuilder();

            // Extract variables for analysis
            Set<String> variables = extractVariables(formula);

            switch (choice) {
                case 1:
                    result.append("Model-Based Analysis:\n");
                    ModelBasedBooleanStrategy modelStrategy = new ModelBasedBooleanStrategy(connector);
                    result.append(modelStrategy.analyzeRelations(formula, variables));
                    break;
                case 2:
                    result.append("Formula-Based Analysis:\n");
                    FormulaBasedBooleanStrategy formulaStrategy = new FormulaBasedBooleanStrategy(connector);
                    result.append(formulaStrategy.analyzeRelations(formula, variables));
                    break;
                case 3:
                    result.append("Model-Based Analysis:\n");
                    modelStrategy = new ModelBasedBooleanStrategy(connector);
                    result.append(modelStrategy.analyzeRelations(formula, variables));
                    result.append("\nFormula-Based Analysis:\n");
                    formulaStrategy = new FormulaBasedBooleanStrategy(connector);
                    result.append(formulaStrategy.analyzeRelations(formula, variables));
                    break;
                default:
                    System.out.println("Invalid choice. Using Formula Structure Analysis.");
                    result.append("Formula-Based Analysis:\n");
                    formulaStrategy = new FormulaBasedBooleanStrategy(connector);
                    result.append(formulaStrategy.analyzeRelations(formula, variables));
            }

            System.out.println("\n" + result.toString());

        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private void handleIntervalStrategy(BoolExpr formula, Scanner scanner) {
        System.out.println("\nInterval analysis not yet implemented.");
    }

    /**
     * Extracts boolean variables from a formula.
     */
    private Set<String> extractVariables(BoolExpr formula) {
        Set<String> variables = new HashSet<>();
        collectVariables(formula, variables);
        return variables;
    }

    private void collectVariables(Expr expr, Set<String> variables) {
        if (expr.isConst() && expr.isBool()) {
            String name = expr.toString();
            if (!name.equals("true") && !name.equals("false")) {
                variables.add(name);
            }
        }
        for (Expr arg : expr.getArgs()) {
            collectVariables(arg, variables);
        }
    }
}

