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
        }
    }
    
}

