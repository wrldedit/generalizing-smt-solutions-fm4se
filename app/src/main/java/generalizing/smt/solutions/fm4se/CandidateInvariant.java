package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;


/**
 * CandidateInvariant encapsulates a candidate invariant.
 * It supports fixed value invariants (e.g., "x = 1"), Boolean invariants (e.g., "p is always true"),
 * and interval invariants (e.g., "y ∈ [l, u]").
 *
 * This class provides factory methods for creation and a method to generate the negation test formula.
 */
public class CandidateInvariant {

    public enum CandidateType {
        FIXED_VALUE,
        BOOLEAN,
        INTERVAL
    }

    private final CandidateType type;
    private final String variableName;
    private final String candidateValue; // For fixed value and Boolean candidates.
    private final String lowerBound; // For interval candidates.
    private final String upperBound; // For interval candidates.

    private CandidateInvariant(CandidateType type, String variableName, String candidateValue, String lowerBound, String upperBound)
    {
        this.type = type;
        this.variableName = variableName;
        this.candidateValue = candidateValue;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }
    
    /**
     * Creates a fixed value invariant (e.g., "x = 1").
     * @param variableName the name of the variable.
     * @param candidateValue the value of the candidate.
     * @param context the Z3 context to create the invariant in.
     * @return a CandidateInvariant representing the fixed value invariant.
     */
    public static CandidateInvariant createFixedValueInvariant(String variableName, String candidateValue, Context context)
    {
        return new CandidateInvariant(CandidateType.FIXED_VALUE, variableName, candidateValue, null, null);
    }

    /**
     * Creates a Boolean invariant (e.g., "p is always true").
     * @param variableName the name of the variable.
     * @param candidateValue the value of the candidate.
     * @param context the Z3 context to create the invariant in.
     * @return a CandidateInvariant representing the Boolean invariant.
     */
    public static CandidateInvariant createBooleanInvariant(String variableName, String candidateValue, Context context)
    {
        return new CandidateInvariant(CandidateType.BOOLEAN, variableName, candidateValue, null, null);
    }

    /**
     * Creates an interval invariant (e.g., "y ∈ [l, u]").
     * @param variableName the name of the variable.
     * @param lowerBound the lower bound of the interval.
     * @param upperBound the upper bound of the interval.
     * @param context the Z3 context to create the invariant in.
     * @return a CandidateInvariant representing the interval invariant.
     */
    public static CandidateInvariant createIntervalInvariant(String variableName, String lowerBound, String upperBound, Context context)
    {
        return new CandidateInvariant(CandidateType.INTERVAL, variableName, null, lowerBound, upperBound);
    }

    /**
     * Creates an automatically determined interval invariant.
     *
     * @param ctx the Z3 context.
     * @return a CandidateInvariant instance for automatic interval discovery.
     */
    public static CandidateInvariant createAutoIntervalInvariant(Context ctx) 
    {
        return new CandidateInvariant(CandidateType.INTERVAL, "AUTO", null, null, null);
    }

    public CandidateType getType()
    {
        return this.type;
    }

    public String getVariableName()
    {
        return this.variableName;
    }

    public String getCandidateValue()
    {
        return this.candidateValue;
    }

    public String getLowerBound()
    {
        return this.lowerBound;
    }

    public String getUpperBound()
    {
        return this.upperBound;
    }

    public BoolExpr generateNegationTestFormula(Context context)
    {
        switch (this.type)
        {
            case FIXED_VALUE:
            // Assume an integer variable; create: x ≠ candidate.
            return context.mkNot(context.mkEq(context.mkConst(variableName, context.getIntSort()), context.mkInt(candidateValue)));
            
            case BOOLEAN:
                // For a Boolean variable: if candidate is true, return x = false; else return x = true.
                BoolExpr boolVar = (BoolExpr) context.mkConst(variableName, context.mkBoolSort());
                if (candidateValue.equalsIgnoreCase("true")) {
                    return context.mkEq(boolVar, context.mkFalse());
                } else {
                    return context.mkEq(boolVar, context.mkTrue());
                }
            case INTERVAL:
                // For an interval candidate: return (y < lower) ∨ (y > upper).
                IntExpr intVar = (IntExpr) context.mkConst(variableName, context.getIntSort());
                BoolExpr lowerViolation = context.mkLt(intVar, context.mkInt(lowerBound));
                BoolExpr upperViolation = context.mkGt(intVar, context.mkInt(upperBound));
                return context.mkOr(lowerViolation, upperViolation);
            default:
                throw new UnsupportedOperationException("Unsupported candidate type");
        }
    }

    @Override
    public String toString()
    {
        switch (this.type)
        {
            case FIXED_VALUE:
                return variableName + " = " + candidateValue;
            case BOOLEAN:
                return variableName + " is always " + candidateValue;
            case INTERVAL:
                return variableName + " ∈ [" + lowerBound + ", " + upperBound + "]";
            default:
                return "Unknown Candidate";
        }
    }




}
