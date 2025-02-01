package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;

public class IntervalFormulaStrategy implements FormulaBasedGeneralizationStrategy {

    private final SMTConnector connector;

    public IntervalFormulaStrategy(SMTConnector connector)
    {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate) {
        Context context = connector.getContext();
        BoolExpr negationTest = candidate.generateNegationTestFormula(context);
        BoolExpr testFormula = context.mkAnd(originalFormula, negationTest);
        String description = "Testing interval candidate invariant: " + candidate.toString() + "\nTest formula: " + testFormula;
        if (connector.isUnsatisfiable(testFormula)) {
            description += "\nResult: Interval invariant holds universally.";
        } else {
            description += "\nResult: Interval invariant does NOT hold.";
        }
        return new GeneralizationResult("Interval Formula Strategy", description);
    }
    
}
