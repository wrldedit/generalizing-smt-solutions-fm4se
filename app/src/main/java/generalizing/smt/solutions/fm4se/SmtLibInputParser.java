package generalizing.smt.solutions.fm4se;

import org.sosy_lab.java_smt.api.SolverContext;
import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.common.ShutdownManager;
import org.sosy_lab.common.configuration.Configuration;
import org.sosy_lab.common.log.BasicLogManager;
import org.sosy_lab.common.log.LogManager;
import org.sosy_lab.java_smt.SolverContextFactory;
import org.sosy_lab.java_smt.SolverContextFactory.Solvers;
import org.sosy_lab.java_smt.api.FormulaManager;
import org.sosy_lab.java_smt.api.BooleanFormula;
import org.sosy_lab.common.configuration.InvalidConfigurationException;

import java.io.IOException;

public class SmtLibInputParser {

    protected final SolverContext context;
    protected final FormulaManager formulaManager;

    public SmtLibInputParser() throws IOException, InvalidConfigurationException 
    {
        Configuration config = Configuration.defaultConfiguration();
        LogManager logger = BasicLogManager.create(config);
        ShutdownManager shutdown = ShutdownManager.create();
        
        this.context = SolverContextFactory.createSolverContext(
            config,
            logger,
            shutdown.getNotifier(),
            Solvers.Z3
        );
        this.formulaManager = context.getFormulaManager();
    }

    /**
     * Parses an SMT-LIB formatted string into a BooleanFormula
     * 
     * @param smtLibInput The SMT-LIB formatted string
     * @return A parsed BooleanFormula
     * @throws IllegalArgumentException if the SMT-LIB input is invalid
     */
    public BooleanFormula parseSmtLibInput(String smtLibInput) 
    {
        try 
        {
            return formulaManager.parse(smtLibInput);
        } 
        catch (Exception e) 
        {
            throw new IllegalArgumentException("Invalid SMT-LIB input: " + e.getMessage(),e);
        }
    }

    /**
     * Validates the parsed formula and checks satisfiability.
     * 
     * @param formula The parsed BooleanFormula to validate
     * @return true if the formula is satisfiable, false otherwise
     */
    public boolean validateFormula(BooleanFormula formula) {
        try (ProverEnvironment prover = context.newProverEnvironment()) {
            prover.addConstraint(formula);
            return !prover.isUnsat();
        } 
        catch (Exception e) 
        {
            throw new RuntimeException("Error validating formula: " + e.getMessage(), e);
        }
    }
    
}
