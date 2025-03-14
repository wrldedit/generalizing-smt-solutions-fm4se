package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

/**
 * CandidateInvariant represents a potential invariant to be verified.
 * This class:
 * 1. Provides factory methods for different types of invariants
 * 2. Manages the Z3 expressions for invariant verification
 * 3. Supports both boolean and integer invariants
 * 
 * Types of invariants supported:
 * - Fixed value invariants (e.g., x = true)
 * - Boolean relationship invariants
 * - Integer range invariants
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public class CandidateInvariant {
    private final BoolExpr invariant;
    private final String description;

    /**
     * Creates a new candidate invariant.
     * 
     * @param invariant The Z3 boolean expression representing the invariant
     * @param description Human-readable description of the invariant
     */
    public CandidateInvariant(BoolExpr invariant, String description) {
        this.invariant = invariant;
        this.description = description;
    }

    /**
     * Gets the Z3 boolean expression for this invariant.
     * 
     * @return The invariant expression
     */
    public BoolExpr getInvariant() {
        return invariant;
    }

    /**
     * Gets the human-readable description of this invariant.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates a fixed value invariant for a boolean variable.
     * Example: "x = true" or "y = false"
     * 
     * @param variableName Name of the variable
     * @param value The boolean value to fix
     * @param ctx The Z3 context
     * @return A new CandidateInvariant
     */
    public static CandidateInvariant createFixedValueInvariant(String variableName, String value, Context ctx) {
        BoolExpr var = ctx.mkBoolConst(variableName);
        BoolExpr val = Boolean.parseBoolean(value) ? ctx.mkTrue() : ctx.mkFalse();
        BoolExpr invariant = ctx.mkEq(var, val);
        String description = String.format("%s = %s", variableName, value);
        return new CandidateInvariant(invariant, description);
    }

    /**
     * Creates an automatic boolean invariant for relationship analysis.
     * This is used when analyzing relationships between boolean variables
     * without specifying a fixed value.
     * 
     * @param ctx The Z3 context
     * @return A new CandidateInvariant
     */
    public static CandidateInvariant createTrue(Context ctx) {
        return new CandidateInvariant(ctx.mkTrue(), "Auto-generated boolean invariant");
    }

    /**
     * Generates a formula that tests if this invariant can be violated.
     */
    public BoolExpr generateNegationTestFormula(Context ctx) {
        return ctx.mkNot(invariant);
    }

    @Override
    public String toString() {
        return description;
    }
}
