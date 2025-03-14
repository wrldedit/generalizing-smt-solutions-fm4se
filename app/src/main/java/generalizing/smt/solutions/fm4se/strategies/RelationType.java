package generalizing.smt.solutions.fm4se.strategies;

/**
 * Defines the types of relationships that can exist between boolean variables.
 */
public enum RelationType {
    ALWAYS_TRUE,    // Variable is always true
    ALWAYS_FALSE,   // Variable is always false
    IMPLICATION,    // var1 implies var2
    EQUIVALENCE     // var1 and var2 are equivalent
} 