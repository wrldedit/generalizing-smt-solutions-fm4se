package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import java.util.Scanner;

/**
 * InteractiveAnalyzer is a class that provides an interactive session for analyzing a given formula.
 */
public class InteractiveAnalyzer {

    private final SMTConnector connector;

    /**
     * Constructs an InteractiveAnalyzer with the given SMTConnector.
     * @param connector the SMTConnector providing the Z3 context and checking methods.
     */
    public InteractiveAnalyzer(SMTConnector connector)
    {
        this.connector = connector;
    }

    public void runInteractiveSession(BoolExpr formula) 
    {
        Context ctx = this.connector.getContext();
        System.out.println("Original formula: " + formula);

        Scanner scanner = new Scanner(System.in);
        while (true) 
        {
            System.out.println("\nSelect a candidate invariant check:");
            System.out.println("1: Fixed Value Invariant");
            System.out.println("2: Boolean Invariant");
            System.out.println("3: Interval Invariant");
            System.out.println("q: Quit");
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("q")) 
            {
                System.out.println("Exiting interactive session.");
                break;
            }

            CandidateInvariant candidate = null;
            FormulaBasedGeneralizationStrategy strategy = null;

            switch (choice) 
            {
                case "1":
                    // Fixed Value Candidate: e.g. x = 1
                    System.out.print("Enter the variable name (e.g., x): ");
                    String variableName = scanner.nextLine().trim();
                    System.out.print("Enter the candidate value for " + variableName + " (true/false): ");
                    String candidateValue = scanner.nextLine().trim();
                    candidate = CandidateInvariant.createFixedValueInvariant(variableName, candidateValue, ctx);
                    strategy = new FixedValueFormulaStrategy(connector);
                    break;
                case "2":
                    // Boolean candidate invariant: e.g., p is always true
                    System.out.print("Enter the Boolean variable name (e.g., p): ");
                    String booleanVariableNameString = scanner.nextLine().trim();
                    System.out.print("Enter the candidate value for " + booleanVariableNameString + " (true/false): ");
                    String booleanCandidateString = scanner.nextLine().trim();
                    candidate = CandidateInvariant.createBooleanInvariant(booleanVariableNameString, booleanCandidateString, ctx);
                    strategy = new AlwaysTrueFalseFormulaStrategy(connector);
                    break;
                case "3":
                    System.out.println("Do you want to determine intervals for a specific variable? (y/n)");
                    String specificVarChoice = scanner.nextLine().trim().toLowerCase();

                    if (specificVarChoice.equals("y")) {
                        System.out.print("Enter the variable name: ");
                        String varName = scanner.nextLine().trim();
                        candidate = CandidateInvariant.createAutoIntervalInvariant(ctx);
                        strategy = new IntervalFormulaStrategy(connector, varName); // Analyze only this variable
                    } else {
                        candidate = CandidateInvariant.createAutoIntervalInvariant(ctx);
                        strategy = new IntervalFormulaStrategy(connector); // Analyze all variables
                    }
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    continue;
            }

            if (candidate != null && strategy != null)
            {
                // Apply the selected strategy to the original formula and the candidate.
                System.out.println("\nVerifying candidate invariant: " + candidate.toString());
                GeneralizationResult result = strategy.apply(formula, candidate);
                System.out.println("Result:\n" + result);
            }
        }
        scanner.close();
    }
    
}

