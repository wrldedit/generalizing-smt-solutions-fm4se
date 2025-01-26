package generalizing.smt.solutions.fm4se;

import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;
import org.sosy_lab.java_smt.api.SolverContext.ProverOptions;

import java.io.IOException;
import java.lang.InterruptedException;

import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.api.*;

public class FormulaExecutor {

    private final SmtLibInputParser parser;

    public FormulaExecutor(SmtLibInputParser parser)
    {
        this.parser = parser;
    }

    /**
     * Retrieve a single solution for the given formula
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



}
