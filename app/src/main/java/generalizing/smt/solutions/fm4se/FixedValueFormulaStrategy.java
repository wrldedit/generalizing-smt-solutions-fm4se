package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

/**
 * FixedValueFormulaStrategy verifies a fixed-value candidate invariant.
 * For a candidate of the form "x = c", it checks whether the augmented formula
 * F ∧ (x ≠ c) is unsatisfiable.
 */
public class FixedValueFormulaStrategy implements FormulaBasedGeneralizationStrategy {

    private final SMTConnector connector;

    /**
     * Constructs a new FixedValueFormulaStrategy with the given SMTConnector.
     * @param connector the SMTConnector to use for checking satisfiability
     */
    public FixedValueFormulaStrategy(SMTConnector connector)
    {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate)
    {
        Context context = connector.getContext();

        //Build the negation test formula from the candidate invariant
        BoolExpr negationTest = candidate.generateNegationTestFormula(context);

        //Augment original formula with the negation test
        BoolExpr testFormula = context.mkAnd(originalFormula, negationTest);

        String description = "Testing fixed value candidate: " + candidate.toString() + "\nTest formula: " + testFormula;

        if (connector.isUnsatisfiable(testFormula))
        {
            description += "\nResult: Invariant holds universally.";
        } 
        else
        {
            description += "\nResult: Invariant does NOT hold.";
        }

        return new GeneralizationResult("Fixed Value Formula Strategy", description);
    }
}


