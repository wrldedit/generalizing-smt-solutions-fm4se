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

    private final SolverContext context;
    private final FormulaManager formulaManager;

    public SmtLibInputParser() throws IOException, InvalidConfigurationException 
    {
        // Create configuration with explicit solver settings
        Configuration config = Configuration.builder()
            .setOption("solver.solver", "z3")
            .setOption("solver.z3.path", detectZ3Path())
            .build();
        
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

    private String detectZ3Path() {
        String osName = System.getProperty("os.name").toLowerCase();
        String userDir = System.getProperty("user.dir");
        
        if (osName.contains("windows")) {
            return "C:/Users/Fritz Trede/z3-4.8.9-x64-win/bin/z3.exe";
        } else if (osName.contains("linux")) {
            return "/usr/bin/z3";
        } else if (osName.contains("mac")) {
            return "/usr/local/bin/z3";
        }
        throw new RuntimeException("Unsupported operating system");
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
    
}
