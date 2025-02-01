package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

/**
 * SMTConnector manages interactions with the SMT solver.
 * It initializes the Z3 context, builds formulas, retrieves models, and supports counterexample queries.
 * 
 * @author Fritz Trede
 * @version 0.1
 * @since since 2025-02-01
 */

public class SMTConnector {

    private Context context;

    public SMTConnector() 
    {
        try 
        {
            this.context = new Context();
        } 
        catch (Z3Exception e) 
        {
            System.err.println("Error initializing Z3 SMT solver: " + e.getMessage());
        }
    }

    /**
     * Returns the Z3 context.
     * @return the Z3 context
     */
    public Context getContext() 
    {
        return this.context;
    }

    /**
     * Returns a single solution for the given formula.
     * @param formula the formula to solve
     * @return a model if the solution exists, null otherwise
     */
    public Model getSolution(BoolExpr formula)
    {
        Solver solver = this.context.mkSolver();
        solver.add(formula);
        if (solver.check() == Status.SATISFIABLE)
        {
            return solver.getModel();
        }
        return null;
    }

    /**
     * Checks if the given formula is satisfiable.
     * @param formula the formula to check
     * @return true if the formula is satisfiable, false otherwise
     */
    public boolean isSatisfiable(BoolExpr formula)
    {
        Solver solver = this.context.mkSolver();
        solver.add(formula);
        return solver.check() == Status.SATISFIABLE;
    }

    /**
     * Checks if the given formula is unsatisfiable.
     * @param formula the formula to check
     * @return true if the formula is unsatisfiable, false otherwise
     */
    public boolean isUnsatisfiable(BoolExpr formula)
    {
        Solver solver = this.context.mkSolver();
        solver.add(formula);
        return solver.check() == Status.UNSATISFIABLE;
    }



    public void close()
    {
        this.context.close();
    }


    
}

