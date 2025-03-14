package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Test suite for integer strategies.
 * Tests both NaiveIntegerStrategy and BinarySearchIntegerStrategy.
 */
public class IntegerStrategyTest {
    private Context ctx;
    private SMTConnector connector;
    private NaiveIntegerStrategy naiveStrategy;
    private BinarySearchIntegerStrategy binaryStrategy;

    @BeforeEach
    void setUp() {
        ctx = new Context();
        connector = new SMTConnector(ctx);
        naiveStrategy = new NaiveIntegerStrategy(connector);
        binaryStrategy = new BinarySearchIntegerStrategy(connector);
    }

    @Test
    @DisplayName("Test exact value constraint")
    void testExactValue() {
        // Create formula: x = 5
        IntExpr x = ctx.mkIntConst("x");
        BoolExpr formula = ctx.mkEq(x, ctx.mkInt(5));

        System.out.println("\nTesting exact value:");
        System.out.println("Formula: " + formula);

        Set<String> variables = new HashSet<>();
        variables.add("x");

        // Test naive strategy
        String naiveResult = naiveStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nNaive strategy result:\n" + naiveResult);
        assertTrue(naiveResult.contains("x: [5, 5]"),
            "Naive strategy should detect x must be exactly 5");

        // Test binary search strategy
        String binaryResult = binaryStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nBinary search strategy result:\n" + binaryResult);
        assertTrue(binaryResult.contains("x: [5, 5]"),
            "Binary search strategy should detect x must be exactly 5");
    }

    @Test
    @DisplayName("Test range constraints")
    void testRange() {
        // Create formula: 0 ≤ x ≤ 10
        IntExpr x = ctx.mkIntConst("x");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkGe(x, ctx.mkInt(0)),
            ctx.mkLe(x, ctx.mkInt(10))
        );

        System.out.println("\nTesting range constraints:");
        System.out.println("Formula: " + formula);

        Set<String> variables = new HashSet<>();
        variables.add("x");

        // Test naive strategy
        String naiveResult = naiveStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nNaive strategy result:\n" + naiveResult);
        assertTrue(naiveResult.contains("x: [0, 10]"),
            "Naive strategy should detect x is between 0 and 10");

        // Test binary search strategy
        String binaryResult = binaryStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nBinary search strategy result:\n" + binaryResult);
        assertTrue(binaryResult.contains("x: [0, 10]"),
            "Binary search strategy should detect x is between 0 and 10");
    }

    @Test
    @DisplayName("Test dependent variables")
    void testDependentVariables() {
        // Create formula: y > x AND x ≥ 0
        IntExpr x = ctx.mkIntConst("x");
        IntExpr y = ctx.mkIntConst("y");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkGt(y, x),
            ctx.mkGe(x, ctx.mkInt(0))
        );

        System.out.println("\nTesting dependent variables:");
        System.out.println("Formula: " + formula);

        Set<String> variables = new HashSet<>();
        variables.add("x");
        variables.add("y");

        // Test naive strategy
        String naiveResult = naiveStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nNaive strategy result:\n" + naiveResult);
        assertTrue(naiveResult.contains("x: [0,"),
            "Naive strategy should detect x's lower bound is 0");
        assertTrue(naiveResult.contains("y:"),
            "Naive strategy should find bounds for y");

        // Test binary search strategy
        String binaryResult = binaryStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nBinary search strategy result:\n" + binaryResult);
        assertTrue(binaryResult.contains("x: [0,"),
            "Binary search strategy should detect x's lower bound is 0");
        assertTrue(binaryResult.contains("y:"),
            "Binary search strategy should find bounds for y");
    }

    @Test
    @DisplayName("Test transitive dependencies")
    void testTransitiveDependencies() {
        // Create formula: x < y < z AND x ≥ 0
        IntExpr x = ctx.mkIntConst("x");
        IntExpr y = ctx.mkIntConst("y");
        IntExpr z = ctx.mkIntConst("z");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkGt(y, x),
            ctx.mkGt(z, y),
            ctx.mkGe(x, ctx.mkInt(0))
        );

        System.out.println("\nTesting transitive dependencies:");
        System.out.println("Formula: " + formula);

        Set<String> variables = new HashSet<>();
        variables.add("x");
        variables.add("y");
        variables.add("z");

        // Test naive strategy
        String naiveResult = naiveStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nNaive strategy result:\n" + naiveResult);
        assertTrue(naiveResult.contains("x: [0,"),
            "Naive strategy should detect x's lower bound is 0");
        assertTrue(naiveResult.contains("y:") && naiveResult.contains("z:"),
            "Naive strategy should find bounds for y and z");

        // Test binary search strategy
        String binaryResult = binaryStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nBinary search strategy result:\n" + binaryResult);
        assertTrue(binaryResult.contains("x: [0,"),
            "Binary search strategy should detect x's lower bound is 0");
        assertTrue(binaryResult.contains("y:") && binaryResult.contains("z:"),
            "Binary search strategy should find bounds for y and z");
    }

    @Test
    @DisplayName("Test tight bounds")
    void testTightBounds() {
        // Create formula: x ≥ 5 AND x ≤ 5
        IntExpr x = ctx.mkIntConst("x");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkGe(x, ctx.mkInt(5)),
            ctx.mkLe(x, ctx.mkInt(5))
        );

        System.out.println("\nTesting tight bounds:");
        System.out.println("Formula: " + formula);

        Set<String> variables = new HashSet<>();
        variables.add("x");

        // Test naive strategy
        String naiveResult = naiveStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nNaive strategy result:\n" + naiveResult);
        assertTrue(naiveResult.contains("x: [5, 5]"),
            "Naive strategy should detect x must be exactly 5");

        // Test binary search strategy
        String binaryResult = binaryStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nBinary search strategy result:\n" + binaryResult);
        assertTrue(binaryResult.contains("x: [5, 5]"),
            "Binary search strategy should detect x must be exactly 5");
    }

    @Test
    @DisplayName("Test arithmetic relationship")
    void testArithmeticRelationship() {
        // Create formula: y = x + 5 AND x ≥ 0
        IntExpr x = ctx.mkIntConst("x");
        IntExpr y = ctx.mkIntConst("y");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkEq(y, ctx.mkAdd(x, ctx.mkInt(5))),
            ctx.mkGe(x, ctx.mkInt(0))
        );

        System.out.println("\nTesting arithmetic relationship:");
        System.out.println("Formula: " + formula);

        Set<String> variables = new HashSet<>();
        variables.add("x");
        variables.add("y");

        // Test naive strategy
        String naiveResult = naiveStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nNaive strategy result:\n" + naiveResult);
        assertTrue(naiveResult.contains("x: [0,"),
            "Naive strategy should detect x's lower bound is 0");
        assertTrue(naiveResult.contains("y:"),
            "Naive strategy should find bounds for y");

        // Test binary search strategy
        String binaryResult = binaryStrategy.analyzeIntegers(formula, variables);
        System.out.println("\nBinary search strategy result:\n" + binaryResult);
        assertTrue(binaryResult.contains("x: [0,"),
            "Binary search strategy should detect x's lower bound is 0");
        assertTrue(binaryResult.contains("y:"),
            "Binary search strategy should find bounds for y");
    }
} 