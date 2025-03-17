package generalizing.smt.solutions.fm4se.performance;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import generalizing.smt.solutions.fm4se.strategies.bool.*;
import generalizing.smt.solutions.fm4se.strategies.integer.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StrategyPerformanceTest {
    private static final int WARMUP_RUNS = 3;  // Increased from 1 to 3
    private static final int TEST_RUNS = 5;    // Increased from 2 to 5
    private static final int[] BOOLEAN_SIZES = {2, 4, 6, 8, 10};  // Keep boolean sizes as is
    private static final int[] INTEGER_SIZES = {1, 2, 3, 4, 5};   // Keep integer sizes as is for now
    private SMTConnector connector;
    private Context ctx;
    
    @BeforeEach
    public void setUp() {
        ctx = new Context();
        connector = new SMTConnector(ctx);
        // Create performance report directory
        new File("build/reports/performance").mkdirs();
    }

    @AfterEach
    public void tearDown() {
        ctx.close();
    }

    @Test
    public void testBooleanStrategiesPerformance() {
        Map<Integer, Long> modelBasedTimes = new HashMap<>();
        Map<Integer, Long> formulaBasedTimes = new HashMap<>();
        
        // Create strategies
        ModelBasedBooleanStrategy modelStrategy = new ModelBasedBooleanStrategy(connector);
        FormulaBasedBooleanStrategy formulaStrategy = new FormulaBasedBooleanStrategy(connector);
        Set<String> variables = new HashSet<>();
        
        // Warmup runs
        System.out.println("Performing warmup runs...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            BoolExpr warmupFormula = generateBooleanFormula(10);
            variables.clear();
            for (int j = 0; j < 10; j++) {
                variables.add("var_" + j);
            }
            modelStrategy.analyzeRelations(warmupFormula, variables);
            formulaStrategy.analyzeRelations(warmupFormula, variables);
        }
        
        // Actual test runs
        System.out.println("\nTesting Boolean Strategies Performance:");
        for (int size : BOOLEAN_SIZES) {
            long modelBasedTotal = 0;
            long formulaBasedTotal = 0;
            
            for (int run = 0; run < TEST_RUNS; run++) {
                BoolExpr formula = generateBooleanFormula(size);
                variables.clear();
                for (int i = 0; i < size; i++) {
                    variables.add("var_" + i);
                }
                
                // Test ModelBased
                long startTime = System.nanoTime();
                modelStrategy.analyzeRelations(formula, variables);
                modelBasedTotal += System.nanoTime() - startTime;
                
                // Test FormulaBased
                startTime = System.nanoTime();
                formulaStrategy.analyzeRelations(formula, variables);
                formulaBasedTotal += System.nanoTime() - startTime;
            }
            
            // Store average times
            modelBasedTimes.put(size, modelBasedTotal / TEST_RUNS);
            formulaBasedTimes.put(size, formulaBasedTotal / TEST_RUNS);
            
            System.out.printf("Size %d - Model-based: %d ms, Formula-based: %d ms%n",
                size,
                modelBasedTimes.get(size) / 1_000_000,
                formulaBasedTimes.get(size) / 1_000_000);
        }
        
        plotResults("Boolean Strategies Performance",
            "Problem Size (number of variables)",
            modelBasedTimes,
            formulaBasedTimes,
            "boolean_performance.png");
    }

    @Test
    public void testIntegerStrategiesPerformance() {
        System.out.println("\nInteger Strategy Performance Test");
        System.out.println("--------------------------------");
        System.out.printf("Warmup runs: %d, Test runs: %d%n", WARMUP_RUNS, TEST_RUNS);
        System.out.println("Problem sizes: " + Arrays.toString(INTEGER_SIZES));

        // Create arrays to store timing results
        long[][] naiveTimes = new long[INTEGER_SIZES.length][TEST_RUNS];
        long[][] binaryTimes = new long[INTEGER_SIZES.length][TEST_RUNS];

        // Warmup phase
        System.out.println("\nWarmup phase...");
        for (int i = 0; i < WARMUP_RUNS; i++) {
            for (int size : INTEGER_SIZES) {
                BoolExpr formula = generateIntegerFormula(size);
                Set<String> variables = extractIntegerVariables(formula);
                
                // Run both strategies
                runIntegerStrategy(new NaiveIntegerStrategy(connector), formula, variables);
                runIntegerStrategy(new BinarySearchIntegerStrategy(connector), formula, variables);
            }
        }

        // Test phase
        System.out.println("\nTest phase...");
        for (int sizeIndex = 0; sizeIndex < INTEGER_SIZES.length; sizeIndex++) {
            int size = INTEGER_SIZES[sizeIndex];
            System.out.printf("\nTesting size %d...%n", size);
            
            for (int run = 0; run < TEST_RUNS; run++) {
                BoolExpr formula = generateIntegerFormula(size);
                Set<String> variables = extractIntegerVariables(formula);
                
                // Time naive strategy
                long startTime = System.nanoTime();
                runIntegerStrategy(new NaiveIntegerStrategy(connector), formula, variables);
                naiveTimes[sizeIndex][run] = System.nanoTime() - startTime;
                
                // Time binary search strategy
                startTime = System.nanoTime();
                runIntegerStrategy(new BinarySearchIntegerStrategy(connector), formula, variables);
                binaryTimes[sizeIndex][run] = System.nanoTime() - startTime;
                
                System.out.printf("Run %d complete%n", run + 1);
            }
        }

        // Print results
        System.out.println("\nResults (average time in milliseconds):");
        System.out.println("Size\tNaive\tBinary\tRatio (Binary/Naive)");
        for (int i = 0; i < INTEGER_SIZES.length; i++) {
            double naiveAvg = Arrays.stream(naiveTimes[i]).average().orElse(0) / 1_000_000.0;
            double binaryAvg = Arrays.stream(binaryTimes[i]).average().orElse(0) / 1_000_000.0;
            System.out.printf("%d\t%.2f\t%.2f\t%.2f%n", 
                INTEGER_SIZES[i], naiveAvg, binaryAvg, binaryAvg / naiveAvg);
        }
    }

    private void runIntegerStrategy(IntegerStrategy strategy, BoolExpr formula, Set<String> variables) {
        strategy.analyzeIntegers(formula, variables);
    }

    private Set<String> extractIntegerVariables(BoolExpr formula) {
        Set<String> variables = new HashSet<>();
        collectIntegerVariables(formula, variables);
        return variables;
    }

    private void collectIntegerVariables(Expr expr, Set<String> variables) {
        if (expr.isConst() && expr.isInt()) {
            String name = expr.toString();
            if (!name.matches("-?\\d+")) {  // Exclude numeric constants
                variables.add(name);
            }
        }
        for (Expr arg : expr.getArgs()) {
            collectIntegerVariables(arg, variables);
        }
    }

    private BoolExpr generateBooleanFormula(int size) {
        List<BoolExpr> vars = new ArrayList<>();
        List<BoolExpr> constraints = new ArrayList<>();
        
        // Create variables
        for (int i = 0; i < size; i++) {
            vars.add(ctx.mkBoolConst("var_" + i));
        }
        
        // Create implications between consecutive variables
        for (int i = 0; i < size - 1; i++) {
            constraints.add(ctx.mkImplies(vars.get(i), vars.get(i + 1)));
        }
        
        // Add some cyclic implications to make it more complex
        for (int i = 0; i < size; i += 2) {
            int next = (i + 3) % size;
            constraints.add(ctx.mkImplies(vars.get(i), vars.get(next)));
        }
        
        return ctx.mkAnd(constraints.toArray(new BoolExpr[0]));
    }

    private BoolExpr generateIntegerFormula(int size) {
        if (size < 1 || size > 10) {
            throw new IllegalArgumentException("Size must be between 1 and 10");
        }

        Context ctx = connector.getContext();
        List<IntExpr> vars = new ArrayList<>();
        List<BoolExpr> constraints = new ArrayList<>();

        // Create variables x_0 through x_(size-1)
        for (int i = 0; i < size; i++) {
            vars.add(ctx.mkIntConst("x_" + i));
        }

        // Add base constraints for each variable with much larger ranges
        for (int i = 0; i < size; i++) {
            // Each variable has a base range, increasing exponentially with much larger base
            constraints.add(ctx.mkGe(vars.get(i), ctx.mkInt(1000 * i)));  // Start from larger values
            constraints.add(ctx.mkLe(vars.get(i), ctx.mkInt(1000 * (int)Math.pow(10, i))));  // Much larger upper bounds
        }

        // Add relationship constraints between variables with much larger gaps
        for (int i = 0; i < size - 1; i++) {
            // Each variable must be less than the next one with a minimum gap
            constraints.add(ctx.mkLt(vars.get(i), vars.get(i + 1)));
            
            // Add a constraint about the maximum difference between consecutive variables
            // Allow for much larger differences between consecutive variables
            constraints.add(
                ctx.mkLt(
                    ctx.mkSub(vars.get(i + 1), vars.get(i)),
                    ctx.mkInt(5000 * (int)Math.pow(5, i))  // Exponentially increasing maximum gap
                )
            );
        }

        // Add additional constraints in groups of 2-3 variables
        for (int i = 0; i < size - 2; i += 3) {
            // Each group of three consecutive variables must satisfy a complex relationship
            IntExpr var1 = vars.get(i);
            IntExpr var2 = vars.get(i + 1);
            IntExpr var3 = vars.get(Math.min(i + 2, size - 1));

            // var3 must be at least five times the sum of var1 and var2
            constraints.add(
                ctx.mkGe(
                    var3,
                    ctx.mkMul(ctx.mkInt(5), ctx.mkAdd(var1, var2))
                )
            );

            // var2 must be less than ten times var1
            constraints.add(
                ctx.mkLt(
                    var2,
                    ctx.mkMul(ctx.mkInt(10), var1)
                )
            );
        }

        // Add some cross-variable constraints for variables further apart
        for (int i = 0; i < size - 3; i += 2) {
            IntExpr var1 = vars.get(i);
            IntExpr var2 = vars.get(i + 3);

            // Add constraint between variables that are further apart
            constraints.add(
                ctx.mkGt(
                    ctx.mkMul(ctx.mkInt(3), var2),
                    ctx.mkAdd(
                        ctx.mkMul(ctx.mkInt(2), var1),
                        ctx.mkInt(1000 * (i + 1))
                    )
                )
            );
        }

        // Add special constraints for the last few variables if we have enough
        if (size >= 7) {
            // x_6 must be related to the average of x_2 and x_4
            constraints.add(
                ctx.mkGt(
                    ctx.mkMul(ctx.mkInt(8), vars.get(6)),
                    ctx.mkAdd(vars.get(2), vars.get(4))
                )
            );
        }

        if (size >= 9) {
            // x_8 must be related to x_5 and x_7
            constraints.add(
                ctx.mkGt(
                    vars.get(8),
                    ctx.mkAdd(
                        ctx.mkMul(ctx.mkInt(2), vars.get(5)),
                        vars.get(7)
                    )
                )
            );
        }

        return ctx.mkAnd(constraints.toArray(new BoolExpr[0]));
    }

    private void plotResults(String title,
                           String xAxisLabel,
                           Map<Integer, Long> strategy1Times,
                           Map<Integer, Long> strategy2Times,
                           String outputFile) {
        XYSeries series1 = new XYSeries("Strategy 1");
        XYSeries series2 = new XYSeries("Strategy 2");
        
        // Use the correct problem sizes based on the test type
        int[] problemSizes = title.contains("Boolean") ? BOOLEAN_SIZES : INTEGER_SIZES;
        
        for (int size : problemSizes) {
            series1.add(size, strategy1Times.get(size) / 1_000_000.0); // Convert to milliseconds
            series2.add(size, strategy2Times.get(size) / 1_000_000.0);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,
            xAxisLabel,
            "Execution Time (ms)",
            dataset
        );
        
        try {
            ChartUtils.saveChartAsPNG(
                new File("build/reports/performance/" + outputFile),
                chart,
                800,
                600
            );
            System.out.println("\nPerformance chart saved as: build/reports/performance/" + outputFile);
        } catch (IOException e) {
            System.err.println("Error saving chart: " + e.getMessage());
        }
    }
} 