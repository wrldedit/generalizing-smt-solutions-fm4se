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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StrategyPerformanceTest {
    private Context ctx;
    private SMTConnector connector;
    
    // Test sizes for measuring performance
    private static final int[] BOOLEAN_PROBLEM_SIZES = {2, 4, 6, 8, 10};
    private static final int[] INTEGER_PROBLEM_SIZES = {1, 2};  // Keep it very small for integer tests
    private static final int WARMUP_RUNS = 1;
    private static final int TEST_RUNS = 2;
    
    @BeforeEach
    public void setup() {
        ctx = new Context();
        connector = new SMTConnector(ctx);
        // Create performance report directory
        new File("build/reports/performance").mkdirs();
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
        for (int size : BOOLEAN_PROBLEM_SIZES) {
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
        Map<Integer, Long> naiveTimes = new HashMap<>();
        Map<Integer, Long> binarySearchTimes = new HashMap<>();
        
        // Create strategies
        NaiveIntegerStrategy naiveStrategy = new NaiveIntegerStrategy(connector);
        BinarySearchIntegerStrategy binarySearchStrategy = new BinarySearchIntegerStrategy(connector);
        Set<String> variables = new HashSet<>();
        
        // Single warmup run with minimum size
        System.out.println("Performing warmup run...");
        BoolExpr warmupFormula = generateIntegerFormula(1);
        variables.clear();
        variables.add("x_0");
        naiveStrategy.analyzeIntegers(warmupFormula, variables);
        binarySearchStrategy.analyzeIntegers(warmupFormula, variables);
        
        // Actual test runs
        System.out.println("\nTesting Integer Strategies Performance:");
        for (int size : INTEGER_PROBLEM_SIZES) {
            long naiveTotal = 0;
            long binarySearchTotal = 0;
            
            for (int run = 0; run < TEST_RUNS; run++) {
                BoolExpr formula = generateIntegerFormula(size);
                variables.clear();
                for (int i = 0; i < size; i++) {
                    variables.add("x_" + i);
                }
                
                // Test Naive
                long startTime = System.nanoTime();
                naiveStrategy.analyzeIntegers(formula, variables);
                naiveTotal += System.nanoTime() - startTime;
                
                // Test BinarySearch
                startTime = System.nanoTime();
                binarySearchStrategy.analyzeIntegers(formula, variables);
                binarySearchTotal += System.nanoTime() - startTime;
            }
            
            // Store average times
            naiveTimes.put(size, naiveTotal / TEST_RUNS);
            binarySearchTimes.put(size, binarySearchTotal / TEST_RUNS);
            
            System.out.printf("Size %d - Naive: %d ms, Binary Search: %d ms%n",
                size,
                naiveTimes.get(size) / 1_000_000,
                binarySearchTimes.get(size) / 1_000_000);
        }
        
        plotResults("Integer Strategies Performance",
            "Problem Size (number of constraints)",
            naiveTimes,
            binarySearchTimes,
            "integer_performance.png");
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
        // Always create minimal constraints like in App.java example
        IntExpr x = ctx.mkIntConst("x_0");
        if (size == 1) {
            return ctx.mkAnd(
                ctx.mkGe(x, ctx.mkInt(0)),
                ctx.mkLe(x, ctx.mkInt(5))
            );
        }
        
        // For size 2, mimic the simple example from App.java
        IntExpr y = ctx.mkIntConst("x_1");
        return ctx.mkAnd(
            ctx.mkGe(x, ctx.mkInt(0)),
            ctx.mkLe(x, ctx.mkInt(5)),
            ctx.mkGt(y, x),
            ctx.mkLt(y, ctx.mkAdd(x, ctx.mkInt(2)))  // Even smaller range than App.java
        );
    }

    private void plotResults(String title,
                           String xAxisLabel,
                           Map<Integer, Long> strategy1Times,
                           Map<Integer, Long> strategy2Times,
                           String outputFile) {
        XYSeries series1 = new XYSeries("Strategy 1");
        XYSeries series2 = new XYSeries("Strategy 2");
        
        // Use the correct problem sizes based on the test type
        int[] problemSizes = title.contains("Boolean") ? BOOLEAN_PROBLEM_SIZES : INTEGER_PROBLEM_SIZES;
        
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