package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

import java.util.List;
import java.util.ArrayList;

/**
 * SolutionEnumerator is a class that enumerates solutions for a given formula.
 * The solutions are enumerated by adding blocking constraints to the formula to avoid duplicates.
 */
public class ModelEnumerator {

    private final SMTConnector connector;

    /**
     * Constructs a new SolutionEnumerator with the given SMTConnector.
     * @param connector the SMTConnector to use
     */
    public ModelEnumerator(SMTConnector connector) 
    {
        this.connector = connector;
    }

    /**
     * Enumerates solutions for the given formula.
     * @param formula the formula to enumerate solutions for
     * @param limit the maximum number of solutions to enumerate
     * @return a list of solutions
     */
    public List<Model> enumerateSolutions(BoolExpr formula, int limit)
    {
        List<Model> models = new ArrayList<>();
        Context context = this.connector.getContext();
        Solver solver = context.mkSolver();
        solver.add(formula);

        while (solver.check() == Status.SATISFIABLE && models.size() < limit)
        {
            Model model = solver.getModel();
            models.add(model);
            
            //Add blocking constraints to avoid duplicates
            BoolExpr blockingConstraint = buildBlockingConstraint(context, model);
            solver.add(blockingConstraint);
        }
        return models;
    }

    /**
     * Builds a blocking constraint for the given model. The blocking constraint prevents the solver from finding the same solution again.
     * @param context the Z3 context
     * @param model the model to build a blocking constraint for
     * @return the blocking constraint
     */
    private BoolExpr buildBlockingConstraint(Context context, Model model)
    {
        List<BoolExpr> constraints = new ArrayList<>();
        for (FuncDecl decl : model.getConstDecls())
        {
            Expr value = model.getConstInterp(decl);

            //Create a constraint that blocks the current solution
            constraints.add(context.mkNot(context.mkEq(context.mkConst(decl.getName(), decl.getRange()),value)));
        }
        return context.mkOr(constraints.toArray(new BoolExpr[0]));
    }
    
}
