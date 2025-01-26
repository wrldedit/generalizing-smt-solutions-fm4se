/*
 * This source file was generated by the Gradle 'init' task
 */
package generalizing.smt.solutions.fm4se;

import org.sosy_lab.java_smt.api.*;
import org.sosy_lab.common.configuration.InvalidConfigurationException;
import org.sosy_lab.java_smt.api.SolverException;

import java.io.IOException;
import java.util.List;

public class App {

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        // Add debugging information
        /* System.out.println("Java Library Path: " + System.getProperty("java.library.path"));
        System.out.println("Working Directory: " + System.getProperty("user.dir"));
        
        try {
            System.loadLibrary("libz3");
            System.out.println("Successfully loaded Z3 library");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load Z3 library: " + e.getMessage());
            System.err.println("Trying to load with absolute path...");
            try {
                System.load("C:/Users/Fritz Trede/z3-4.8.9-x64-win/bin/libz3.dll");
                System.out.println("Successfully loaded Z3 library with absolute path");
            } catch (UnsatisfiedLinkError e2) {
                System.err.println("Failed to load Z3 library with absolute path: " + e2.getMessage());
            }
        }
        
        System.out.println("Hello World!");
        // Print the greeting   
       System.out.println("Testing Java SMT API...");
        try {
           
            // Initialize the configuration, logger, and shutdown manager
            Configuration config = Configuration.fromCmdLineArguments(args);
		    LogManager logger = BasicLogManager.create(config);
		    ShutdownManager shutdown = ShutdownManager.create();
            
            //Use (Z3) SMT Solver + Formula Managers
		    SolverContext context = SolverContextFactory.createSolverContext(config, 
                logger, 
                shutdown.getNotifier(), 
                Solvers.Z3
            );



            // Create the formula manager
            FormulaManager fmgr = context.getFormulaManager();
            IntegerFormulaManager imgr = fmgr.getIntegerFormulaManager();

            // Define variables
            IntegerFormula x = imgr.makeVariable("x");
            IntegerFormula y = imgr.makeVariable("y");

            // Define a formula: x + y = 10
            BooleanFormula constraint = imgr.equal(imgr.add(x, y), imgr.makeNumber(10));

            // Before using the prover, enable model generation
            try (ProverEnvironment prover = context.newProverEnvironment(
                ProverOptions.GENERATE_MODELS
            )) {
                prover.addConstraint(constraint);
                boolean isUnsat = prover.isUnsat();
                
                if (!isUnsat) {
                    Model model = prover.getModel();
                    // Print the model
                    System.out.println(model);
                }
                else {
                    System.out.println("No solution exists.");
                }
            }

            // Cleanup
            context.close();
        } catch (Exception e) {
            e.printStackTrace();
        } */

        try 
        {
            SmtLibInputParser parser = new SmtLibInputParser();

            String smtLibInput = 
                "(declare-const x Int)\n" +
                "(declare-const y Int)\n" +
                "(assert (and (= (+ x y) 10) (and (< x 5) (< y 10))))";

            BooleanFormula formula = parser.parseSmtLibInput(smtLibInput);
            boolean isSat = parser.validateFormula(formula);
            System.out.println("Formula satisfiable? " + isSat);

            FormulaExecutor executor = new FormulaExecutor(parser);
            List <Model> models = executor.getBoundedSolutions(smtLibInput, 10);
            
            for (Model model : models)
            {
                System.out.println("Solution: " + model);
            }
        }
        catch (IOException e)
        {
            System.err.println("Error initializing SMT solver: " + e.getMessage());
        }
        catch (IllegalArgumentException e)
        {
            System.err.println("Invalid input: " + e.getMessage());
        }
        catch (InvalidConfigurationException e)
        {
            System.err.println("Error initializing SMT solver Configuration: " + e.getMessage());
        }
        catch (SolverException | InterruptedException e) {
            System.err.println("Error during solving: " + e.getMessage());
        }
    }
}


