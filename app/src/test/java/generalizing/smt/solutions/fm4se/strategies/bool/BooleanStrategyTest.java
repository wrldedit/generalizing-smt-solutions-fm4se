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

    // Fixed Value Tests

    @Test
    @DisplayName("Test direct fixed value: variable must be true")
    void testDirectFixedValueTrue() {
        // Formula: p
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr formula = p;

        Set<String> variables = new HashSet<>();
        variables.add("p");
        
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p is always true"), 
            "Model strategy should detect p must be true");
        
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p is always true"),
            "Formula strategy should detect p must be true");
    }

    @Test
    @DisplayName("Test direct fixed value: variable must be false")
    void testDirectFixedValueFalse() {
        // Formula: NOT p
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr formula = ctx.mkNot(p);

        Set<String> variables = new HashSet<>();
        variables.add("p");
        
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p is always false"), 
            "Model strategy should detect p must be false");
        
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p is always false"),
            "Formula strategy should detect p must be false");
    }

    @Test
    @DisplayName("Test indirect fixed value through conjunction")
    void testIndirectFixedValueConjunction() {
        // Formula: (p OR q) AND (NOT p)
        // q must be true because p is false and (p OR q) must be true
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkAnd(ctx.mkOr(p, q), ctx.mkNot(p));

        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p is always false"), 
            "Model strategy should detect p must be false");
        assertTrue(modelResult.contains("q is always true"), 
            "Model strategy should detect q must be true");
        
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p is always false"),
            "Formula strategy should detect p must be false");
        assertTrue(formulaResult.contains("q is always true"),
            "Formula strategy should detect q must be true");
    }

    @Test
    @DisplayName("Test fixed value propagation through implication")
    void testFixedValuePropagation() {
        // Formula: p AND (p -> q)
        // p is true directly, q becomes true through implication
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkAnd(p, ctx.mkImplies(p, q));

        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        assertTrue(modelResult.contains("p is always true"), 
            "Model strategy should detect p must be true");
        assertTrue(modelResult.contains("q is always true"), 
            "Model strategy should detect q must be true");
        
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);
        assertTrue(formulaResult.contains("p is always true"),
            "Formula strategy should detect p must be true");
        assertTrue(formulaResult.contains("q is always true"),
            "Formula strategy should detect q must be true");
    }

    // Implication Tests

    @Test
    @DisplayName("Test direct implication p → q")
    void testDirectImplication() {
        // Formula: p -> q
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkImplies(p, q);

        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");

        // Test both strategies
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);

        // Verify no fixed values
        assertFalse(modelResult.contains("is always"), 
            "Model strategy should not find fixed values");
        assertFalse(formulaResult.contains("is always"), 
            "Formula strategy should not find fixed values");

        // Verify implication is detected
        assertTrue(modelResult.contains("p = true implies q = true"), 
            "Model strategy should detect p implies q");
        assertTrue(formulaResult.contains("p = true implies q = true"), 
            "Formula strategy should detect p implies q");
    }

    @Test
    @DisplayName("Test implication with negated implying variable ¬p → q")
    void testNegatedImplication() {
        // Formula: (NOT p) -> q
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr formula = ctx.mkImplies(ctx.mkNot(p), q);

        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");

        // Test both strategies
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);

        // Verify no fixed values
        assertFalse(modelResult.contains("is always"), 
            "Model strategy should not find fixed values");
        assertFalse(formulaResult.contains("is always"), 
            "Formula strategy should not find fixed values");

        // Verify implication is detected
        assertTrue(modelResult.contains("p = false implies q = true"), 
            "Model strategy should detect not p implies q");
        assertTrue(formulaResult.contains("p = false implies q = true"), 
            "Formula strategy should detect not p implies q");
    }

    @Test
    @DisplayName("Test transitive implications p → q → r")
    void testTransitiveImplications() {
        // Formula: (p -> q) AND (q -> r)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr r = ctx.mkBoolConst("r");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkImplies(p, q),
            ctx.mkImplies(q, r)
        );

        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        variables.add("r");

        // Test both strategies
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);

        // Verify implications for model strategy
        assertTrue(modelResult.contains("p = true implies all of {q, r} = true"), 
            "Model strategy should detect p implies both q and r");
        assertTrue(modelResult.contains("q = true implies r = true"), 
            "Model strategy should detect q implies r");

        // Verify implications for formula strategy
        assertTrue(formulaResult.contains("p = true implies all of {q, r} = true"), 
            "Formula strategy should detect p implies both q and r");
        assertTrue(formulaResult.contains("q = true implies r = true"), 
            "Formula strategy should detect q implies r");
    }

    @Test
    @DisplayName("Test cyclic implications p → q → r → p")
    void testCyclicImplications() {
        // Formula: (p -> q) AND (q -> r) AND (r -> p)
        BoolExpr p = ctx.mkBoolConst("p");
        BoolExpr q = ctx.mkBoolConst("q");
        BoolExpr r = ctx.mkBoolConst("r");
        BoolExpr formula = ctx.mkAnd(
            ctx.mkImplies(p, q),
            ctx.mkImplies(q, r),
            ctx.mkImplies(r, p)
        );

        Set<String> variables = new HashSet<>();
        variables.add("p");
        variables.add("q");
        variables.add("r");

        // Test both strategies
        String modelResult = modelStrategy.analyzeRelations(formula, variables);
        String formulaResult = formulaStrategy.analyzeRelations(formula, variables);

        // Verify implications for model strategy
        assertTrue(modelResult.contains("p = true implies all of {q, r} = true"), 
            "Model strategy should detect p implies both q and r");
        assertTrue(modelResult.contains("q = true implies all of {p, r} = true"), 
            "Model strategy should detect q implies both p and r");
        assertTrue(modelResult.contains("r = true implies all of {p, q} = true"), 
            "Model strategy should detect r implies both p and q");

        // Verify implications for formula strategy
        assertTrue(formulaResult.contains("p = true implies all of {q, r} = true"), 
            "Formula strategy should detect p implies both q and r");
        assertTrue(formulaResult.contains("q = true implies all of {p, r} = true"), 
            "Formula strategy should detect q implies both p and r");
        assertTrue(formulaResult.contains("r = true implies all of {p, q} = true"), 
            "Formula strategy should detect r implies both p and q");
    }
} 