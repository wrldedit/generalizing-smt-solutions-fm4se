package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.SMTConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Test suite for boolean strategies.
 * Tests both ModelBasedBooleanStrategy and FormulaBasedBooleanStrategy.
 */
public class BooleanStrategyTest {
    private Context ctx;
    private SMTConnector connector;
    private ModelBasedBooleanStrategy modelStrategy;
    private FormulaBasedBooleanStrategy formulaStrategy;

    @BeforeEach
    void setUp() {
        ctx = new Context();
        connector = new SMTConnector(ctx);
        modelStrategy = new ModelBasedBooleanStrategy(connector);
        formulaStrategy = new FormulaBasedBooleanStrategy(connector);
    }

    @Test
    @DisplayName("Test fixed value: variable must be true")
    void testFixedValueTrue() {
        // Create formula: p AND (p OR q)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkAnd(p, ctx.mkOr(p, q));

        // Test model-based strategy
        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p = true"), 
            "Model-based strategy should detect p must be true");

        // Test formula-based strategy
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p = true"),
            "Formula-based strategy should detect p must be true");
    }

    @Test
    @DisplayName("Test fixed value: variable must be false")
    void testFixedValueFalse() {
        // Create formula: (NOT p) AND (p OR q)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkAnd(ctx.mkNot(p), ctx.mkOr(p, q));

        // Test model-based strategy
        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p = false"), 
            "Model-based strategy should detect p must be false");
        assertTrue(modelResult.contains("q = true"), 
            "Model-based strategy should detect q must be true");

        // Test formula-based strategy
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p = false"),
            "Formula-based strategy should detect p must be false");
        assertTrue(formulaResult.contains("q = true"),
            "Formula-based strategy should detect q must be true");
    }

    @Test
    @DisplayName("Test implication: p implies q")
    void testSimpleImplication() {
        // Create formula: p AND (p -> q)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkAnd(p, ctx.mkImplies(p, q));

        // Test model-based strategy
        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p = true"), 
            "Model-based strategy should detect p must be true");
        assertTrue(modelResult.contains("q = true"), 
            "Model-based strategy should detect q must be true");

        // Test formula-based strategy
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p = true"),
            "Formula-based strategy should detect p must be true");
        assertTrue(formulaResult.contains("q = true"),
            "Formula-based strategy should detect q must be true");
    }

    @Test
    @DisplayName("Test transitive implications")
    void testTransitiveImplications() {
        // Create formula: p AND (p -> q) AND (q -> r)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr r = ctx.mkBoolConst("r");
        BoolExpr formula = ctx.mkAnd(
            p,
            ctx.mkImplies(p, q),
            ctx.mkImplies(q, r)
        );

        // Test model-based strategy
        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        variables.add("r");
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p = true"), 
            "Model-based strategy should detect p must be true");
        assertTrue(modelResult.contains("q = true"), 
            "Model-based strategy should detect q must be true");
        assertTrue(modelResult.contains("r = true"), 
            "Model-based strategy should detect r must be true");

        // Test formula-based strategy
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p = true"),
            "Formula-based strategy should detect p must be true");
        assertTrue(formulaResult.contains("q = true"),
            "Formula-based strategy should detect q must be true");
        assertTrue(formulaResult.contains("r = true"),
            "Formula-based strategy should detect r must be true");
    }

    @Test
    @DisplayName("Test cyclic implications")
    void testCyclicImplications() {
        // Create formula: p AND (p -> q) AND (q -> r) AND (r -> p)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr r = ctx.mkBoolConst("r");
        BoolExpr formula = ctx.mkAnd(
            p,
            ctx.mkImplies(p, q),
            ctx.mkImplies(q, r),
            ctx.mkImplies(r, p)
        );

        // Test model-based strategy
        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        variables.add("r");
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p = true"), 
            "Model-based strategy should detect p must be true");
        assertTrue(modelResult.contains("q = true"), 
            "Model-based strategy should detect q must be true");
        assertTrue(modelResult.contains("r = true"), 
            "Model-based strategy should detect r must be true");

        // Test formula-based strategy
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p = true"),
            "Formula-based strategy should detect p must be true");
        assertTrue(formulaResult.contains("q = true"),
            "Formula-based strategy should detect q must be true");
        assertTrue(formulaResult.contains("r = true"),
            "Formula-based strategy should detect r must be true");
    }

    @Test
    @DisplayName("Test formula with no fixed values or implications")
    void testNoConstraints() {
        // Create formula: p OR q
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkOr(p, q);

        // Test model-based strategy
        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertFalse(modelResult.contains("= true") || modelResult.contains("= false"), 
            "Model-based strategy should not detect any fixed values");

        // Test formula-based strategy
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertFalse(formulaResult.contains("= true") || formulaResult.contains("= false"),
            "Formula-based strategy should not detect any fixed values");
    }
} 