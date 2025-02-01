package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

/**
 * AlwaysTrueFalseFormulaStrategy verifies Boolean candidate invariants.
 * For a candidate like "p is always true" (or false), it checks if the augmented formula
 * F ∧ (p = opposite) is unsatisfiable.
 */
public class AlwaysTrueFalseFormulaStrategy implements FormulaBasedGeneralizationStrategy {

    private final SMTConnector connector;

    /**
     * Constructs a new AlwaysTrueFalseFormulaStrategy with the given SMTConnector.
     * @param connector the SMTConnector to use for checking satisfiability
     */
    public AlwaysTrueFalseFormulaStrategy(SMTConnector connector)
    {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate)
    {
        Context context = connector.getContext();
        BoolExpr negationTest = candidate.generateNegationTestFormula(context);
        BoolExpr testFormula = context.mkAnd(originalFormula, negationTest);
        String description = "Testing Boolean candidate invariant: " + candidate.toString() + "\nTest formula: " + testFormula;
        if (connector.isUnsatisfiable(testFormula)) {
            description += "\nResult: Boolean invariant holds universally.";
        } else {
            description += "\nResult: Boolean invariant does NOT hold.";
        }
        return new GeneralizationResult("Always True/False Formula Strategy", description);
    }
    

}

