package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

/**
 * SMTConnector provides a unified interface to Z3 SMT solver functionality.
 * This class:
 * 1. Manages the Z3 context and solver instances
 * 2. Provides high-level methods for formula manipulation
 * 3. Handles model generation and satisfiability checks
 * 
 * All interactions with Z3 should go through this class to ensure:
 * - Consistent context management
 * - Proper resource cleanup
 * - Standardized error handling
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public class SMTConnector {
    private final Context context;
    private final Solver solver;

    /**
     * Creates a new SMTConnector with a given Z3 context.
     * 
     * @param context The Z3 context to use
     */
    public SMTConnector(Context context) {
        this.context = context;
        this.solver = context.mkSolver();
    }

    /**
     * Gets the Z3 context.
     * 
     * @return The Z3 context
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets the Z3 solver.
     * 
     * @return The Z3 solver
     */
    public Solver getSolver() {
        return solver;
    }

    /**
     * Checks if a formula is satisfiable.
     * Uses solver push/pop for isolation.
     * 
     * @param formula The formula to check
     * @return Status.SATISFIABLE if satisfiable, Status.UNSATISFIABLE if not
     */
    public Status check(BoolExpr formula) {
        solver.push();
        solver.add(formula);
        Status result = solver.check();
        solver.pop();
        return result;
    }

    /**
     * Gets a model for a satisfiable formula.
     * Uses solver push/pop for isolation.
     * 
     * @param formula The formula to get a model for
     * @return A model if the formula is satisfiable, null otherwise
     */
    public Model getModel(BoolExpr formula) {
        solver.push();
        solver.add(formula);
        Model model = null;
        if (solver.check() == Status.SATISFIABLE) {
            model = solver.getModel();
        }
        solver.pop();
        return model;
    }

    /**
     * Returns a single solution for the given formula.
     * Creates a new solver instance for thread safety.
     * 
     * @param formula the formula to solve
     * @return a model if the solution exists, null otherwise
     */
    public Model getSolution(BoolExpr formula) {
        Solver solver = this.context.mkSolver();
        solver.add(formula);
        if (solver.check() == Status.SATISFIABLE) {
            return solver.getModel();
        }
        return null;
    }

    /**
     * Checks if the given formula is satisfiable.
     * Creates a new solver instance for thread safety.
     * 
     * @param formula the formula to check
     * @return true if the formula is satisfiable, false otherwise
     */
    public boolean isSatisfiable(BoolExpr formula) {
        Solver solver = this.context.mkSolver();
        solver.add(formula);
        return solver.check() == Status.SATISFIABLE;
    }

    /**
     * Checks if the given formula is unsatisfiable.
     * Creates a new solver instance for thread safety.
     * 
     * @param formula the formula to check
     * @return true if the formula is unsatisfiable, false otherwise
     */
    public boolean isUnsatisfiable(BoolExpr formula) {
        Solver solver = this.context.mkSolver();
        solver.add(formula);
        return solver.check() == Status.UNSATISFIABLE;
    }

    /**
     * Closes the Z3 context and releases resources.
     * Should be called when the connector is no longer needed.
     */
    public void close() {
        this.context.close();
    }
}

