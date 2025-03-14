package generalizing.smt.solutions.fm4se.strategies;

/**
 * Represents a relationship between boolean variables.
 */
public class BooleanRelation {
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

    public String getVar1() {
        return var1;
    }

    public String getVar2() {
        return var2;
    }

    public RelationType getType() {
        return type;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        switch (type) {
            case ALWAYS_TRUE:
                return var1 + " is always true";
            case ALWAYS_FALSE:
                return var1 + " is always false";
            case IMPLICATION:
                return var1 + " -> " + var2;
            case EQUIVALENCE:
                return var1 + " <-> " + var2;
            default:
                return "Unknown relation between " + var1 + " and " + var2;
        }
    }
} 