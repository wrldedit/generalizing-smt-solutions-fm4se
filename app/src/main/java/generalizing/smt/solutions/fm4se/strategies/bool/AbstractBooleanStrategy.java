package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.*;
import generalizing.smt.solutions.fm4se.*;
import generalizing.smt.solutions.fm4se.strategies.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract base class for boolean analysis strategies.
 * Provides common functionality and defines the interface for concrete strategies.
 */
public abstract class AbstractBooleanStrategy implements FormulaBasedGeneralizationStrategy {
    protected final SMTConnector connector;

    /**
     * Creates a new AbstractBooleanStrategy.
     * @param connector The SMT solver connector
     */
    protected AbstractBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    /**
     * Gets the name of the strategy.
     * @return The strategy name
     */
    protected abstract String getStrategyName();

    /**
     * Analyzes relationships between boolean variables in the formula.
     * @param formula The formula to analyze
     * @param variables The set of variables to consider
     * @param ctx The Z3 context
     * @return List of discovered boolean relationships
     */
    protected abstract List<BooleanRelation> analyzeRelations(BoolExpr formula, Set<String> variables, Context ctx);

    /**
     * Common relationship types found in boolean formulas.
     */
    protected enum RelationType {
        IMPLICATION("->", "implies"),
        EQUIVALENCE("<->", "equivalent to"),
        MUTUAL_EXCLUSION("XOR", "mutually exclusive with"),
        ALWAYS_TRUE("true", "is always true"),
        ALWAYS_FALSE("false", "is always false"),
        INDEPENDENT("||", "independent of");

        private final String symbol;
        private final String description;

        RelationType(String symbol, String description) {
            this.symbol = symbol;
            this.description = description;
        }

        public String getSymbol() { return symbol; }
        public String getDescription() { return description; }
    }

    /**
     * Represents a relationship between boolean variables.
     */
    protected static class BooleanRelation {
        private final String var1;
        private final String var2;
        private final RelationType type;
        private final double confidence;

        public BooleanRelation(String var1, String var2, RelationType type, double confidence) {
            this.var1 = var1;
            this.var2 = var2;
            this.type = type;
            this.confidence = confidence;
        }

        @Override
        public String toString() {
            if (var2 == null) {
                return String.format("%s %s (confidence: %.2f%%)", 
                    var1, type.getDescription(), confidence * 100);
            }
            return String.format("%s %s %s (confidence: %.2f%%)", 
                var1, type.getDescription(), var2, confidence * 100);
        }
    }

    @Override
    public GeneralizationResult apply(BoolExpr formula, CandidateInvariant candidate) {
        Set<String> variables = new HashSet<>();
        for (Expr arg : formula.getArgs()) {
            if (arg.isConst() && arg.isBool()) {
                String name = arg.toString();
                if (!name.equals("true") && !name.equals("false")) {
                    variables.add(name);
                }
            }
        }

        List<BooleanRelation> relations = analyzeRelations(formula, variables, connector.getContext());
        StringBuilder result = new StringBuilder();
        
        if (relations.isEmpty()) {
            result.append("No patterns found in the solution space.");
        } else {
            result.append("Patterns found in the solution space:\n\n");
            
            // First show fixed values
            List<BooleanRelation> fixedValues = relations.stream()
                .filter(r -> r.type == RelationType.ALWAYS_TRUE || r.type == RelationType.ALWAYS_FALSE)
                .collect(Collectors.toList());
            if (!fixedValues.isEmpty()) {
                result.append("Fixed Values:\n");
                fixedValues.forEach(r -> result.append("  ").append(r).append("\n"));
            }

            // Then show relationships
            List<BooleanRelation> relationships = relations.stream()
                .filter(r -> r.type != RelationType.ALWAYS_TRUE && r.type != RelationType.ALWAYS_FALSE)
                .collect(Collectors.toList());
            if (!relationships.isEmpty()) {
                result.append("\nRelationships:\n");
                relationships.forEach(r -> result.append("  ").append(r).append("\n"));
            }
        }

        return new GeneralizationResult(getStrategyName(), result.toString());
    }

    /**
     * Checks if a formula is a tautology.
     */
    protected boolean isTautology(BoolExpr expr) {
        return connector.isUnsatisfiable(connector.getContext().mkNot(expr));
    }

    /**
     * Creates a variable assignment formula.
     */
    protected BoolExpr createAssignment(String var, boolean value, Context ctx) {
        BoolExpr varExpr = ctx.mkBoolConst(var);
        return value ? varExpr : ctx.mkNot(varExpr);
    }
} 