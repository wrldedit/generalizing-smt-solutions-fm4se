/**
 * Main application class demonstrating the use of SMT solvers for formula analysis.
 * This class provides examples of both boolean and integer formula analysis using
 * the interactive analyzer.
 */
package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.strategies.bool.*;
import java.util.*;

public class App {
    /**
     * Demonstrates boolean formula analysis with a simple disjunctive formula.
     * The formula (p OR q) AND (!q OR r) creates relationships where:
     * - At least one of p or q must be true
     * - If q is true, then r must be true
     * @param ctx The Z3 context for creating expressions
     */
    public static void testBooleanFormula(Context ctx) {
        // Create boolean variables
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr r = ctx.mkBoolConst("r");

        // Create a formula: (p OR q) AND (!q OR r)
        // This formula means:
        // 1. Either p or q (or both) must be true
        // 2. Either q is false OR r must be true
        // This creates clear relationships where:
        // - If q is false, then p must be true (from clause 1)
        // - If q is true, then r must be true (from clause 2)
        BoolExpr formula = ctx.mkAnd(
            ctx.mkOr(p, q),      // p OR q
            ctx.mkOr(ctx.mkNot(q), r)  // !q OR r (equivalent to q => r)
        );

        System.out.println("\n=== Testing Boolean Formula ===");
        System.out.println("Simple Disjunctive Formula:");
        System.out.println("1. Either p or q (or both) must be true (p OR q)");
        System.out.println("2. If q is true, then r must be true (!q OR r)");
        System.out.println("\nExpected properties:");
        System.out.println("- At least one of p or q must be true");
        System.out.println("- If q is true, then r must also be true");
        System.out.println("- p and r can be true regardless of other variables");
        System.out.println("\nValid combinations:");
        System.out.println("- p=true, q=false, r=false  (satisfies both clauses)");
        System.out.println("- p=true, q=true, r=true    (satisfies both clauses)");
        System.out.println("- p=false, q=true, r=true   (satisfies both clauses)");
        System.out.println("\nOriginal formula: " + formula);

        // Launch interactive analyzer for boolean formula
        SMTConnector connector = new SMTConnector(ctx);
        InteractiveAnalyzer analyzer = new InteractiveAnalyzer(connector);
        analyzer.runInteractiveSession(formula);
    }

    /**
     * Demonstrates integer formula analysis with simple bounds and relationships.
     * The formula creates constraints where:
     * - x must be between 0 and 5
     * - y must be greater than x
     * @param ctx The Z3 context for creating expressions
     */
    public static void testIntegerFormula(Context ctx) {
        // Create integer variables
        IntExpr x = ctx.mkIntConst("x");
        IntExpr y = ctx.mkIntConst("y");

        // Create a simple formula:
        // 1. x must be between 0 and 5: 0 <= x <= 5
        // 2. y must be greater than x: y > x
        BoolExpr formula = ctx.mkAnd(
            ctx.mkGe(x, ctx.mkInt(0)),  // x >= 0
            ctx.mkLe(x, ctx.mkInt(5)),  // x <= 5
            ctx.mkGt(y, x)              // y > x
        );

        System.out.println("\n=== Testing Integer Formula ===");
        System.out.println("Simple Integer Formula:");
        System.out.println("1. x must be between 0 and 5: 0 <= x <= 5");
        System.out.println("2. y must be greater than x: y > x");
        System.out.println("\nExpected properties:");
        System.out.println("- x is bounded between 0 and 5");
        System.out.println("- y must be greater than x (no upper bound)");
        System.out.println("- For each value of x, y has a specific minimum value (x + 1)");
        System.out.println("\nOriginal formula: " + formula);

        // Launch interactive analyzer for integer formula
        SMTConnector connector = new SMTConnector(ctx);
        InteractiveAnalyzer analyzer = new InteractiveAnalyzer(connector);
        analyzer.runInteractiveSession(formula);
    }

    /**
     * Main entry point of the application.
     * Creates a Z3 context and runs both boolean and integer formula tests.
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        try (Context ctx = new Context()) {
            testBooleanFormula(ctx);
            testIntegerFormula(ctx);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


