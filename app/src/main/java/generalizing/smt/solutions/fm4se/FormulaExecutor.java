package generalizing.smt.solutions.fm4se;

import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import java.lang.InterruptedException;
import java.util.ArrayList;
import java.util.List;

import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.java_smt.api.Model.ValueAssignment;
import org.sosy_lab.java_smt.api.BooleanFormula;


public class FormulaExecutor {

    private final SmtLibInputParser parser;

    public FormulaExecutor(SmtLibInputParser parser)
    {
        this.parser = parser;
    }

    /**
     * Get a single solution for the given formula
     * 
     * @param smtLibInput the SMT-LIB formula string.
     * @return A single model or null if no solution exists.
     * @throws SolverException Solver exception during solving.
     * @throws InterruptedException if the solver is interrupted while solving.
     */
    public Model getSingleSolution(String smtLibInput) throws SolverException, InterruptedException
    {
        try (ProverEnvironment prover = parser.context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
            BooleanFormula formula = parser.parseSmtLibInput(smtLibInput);
            prover.addConstraint(formula);

            if (!prover.isUnsat()) 
            {
                return prover.getModel();
            }
            else 
            {
                return null; // No solution exists
            }
        }
        catch (SolverException e)
        {
            throw new SolverException("Solver exception: " + e.getMessage(), e);
        }
        catch (InterruptedException e)
        {
            throw new InterruptedException("Solver interrupted: " + e.getMessage());
        }
    }
    

    // TODO: Think about how to steer method behaviour for formulas solution spaces that too large

    /**
     * Get all solutions for the given formula
     * 
     * @param smtLibInput the SMT-LIB formula string.
     * @return A list of models (solutions)
     * @throws SolverException Solver exception during solving.
     * @throws InterruptedException if the solver is interrupted while solving.
     */
    public List<Model> getAllSolutions(String smtLibInput) throws SolverException, InterruptedException
    {
        List<Model> solutions = new ArrayList<>();

        try (ProverEnvironment prover = parser.context.newProverEnvironment(ProverOptions.GENERATE_MODELS)) {
            BooleanFormula formula = parser.parseSmtLibInput(smtLibInput);
            prover.addConstraint(formula);

            while (!prover.isUnsat())
            {
                Model model = prover.getModel();
                solutions.add(model);

                // Build blocking formula from model assignments
                BooleanFormula blocking = createBlockingFormula(model);
                prover.addConstraint(blocking);
            }
        }

        return solutions;
    }

    /**
     * Get a bounded number of solutions for the given formula.
     * Useful for formulas with large solution spaces.
     * 
     * @param smtLibInput formula string.
     * @param maxSolutions the maximum number of solutions to return.
     * @return A list of models (solutions), limited to maxSolutions.
     * @throws SolverException Solver exception during solving.
     * @throws InterruptedException if the solver is interrupted while solving.
     */
    public List<Model> getBoundedSolutions(String smtLibInput, int maxSolutions) throws SolverException, InterruptedException
    {
        List<Model> solutions = new ArrayList<>();

        try (ProverEnvironment prover = parser.context.newProverEnvironment(ProverOptions.GENERATE_MODELS))
        {
            BooleanFormula formula = parser.parseSmtLibInput(smtLibInput);
            prover.addConstraint(formula);

            int count = 0;

            while (!prover.isUnsat() && count < maxSolutions)
            {
                Model model = prover.getModel();
                solutions.add(model);

                BooleanFormula blocking = createBlockingFormula(model);
                prover.addConstraint(blocking);
                count++;
            }
        }

        return solutions;
    }

    /**
     * Create a blocking formula from a model.
     * 
     * @param model the model to create a blocking formula from.
     * @return A blocking formula.
     */
    private BooleanFormula createBlockingFormula(Model model) {
        BooleanFormulaManager bfmgr = parser.formulaManager.getBooleanFormulaManager();
        List<BooleanFormula> disjunctions = new ArrayList<>();

        for (ValueAssignment assignment : model) {
            BooleanFormula term = assignment.getAssignmentAsFormula();
            disjunctions.add(bfmgr.not(term));
        }

        return bfmgr.or(disjunctions);
    }

    /**
     * Helper method to print solutions for debugging purposes
     */

     public void printSolutions(List<Model> solutions)
     {
        if (solutions.isEmpty())
        {
            System.out.println("No solutions found.");
        }
        else
        {
            System.out.println("Solutions found: " + solutions.size());
            for (Model model : solutions)
            {
                System.out.println(model.toString());
            }
        }
     }

}
