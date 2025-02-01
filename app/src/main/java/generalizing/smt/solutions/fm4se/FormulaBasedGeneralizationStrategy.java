package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.BoolExpr;

/**
 * FormulaBasedGeneralizationStrategy defines an interface for strategies that
 * verify candidate invariants purely by operating on formulas.
 */
public interface FormulaBasedGeneralizationStrategy {

    /**
     * Applies the strategy to verify the candidate invariant on the original formula.
     * @param originalFormula the original formula to be generalized.
     * @param candidate the candidate invariant to be applied.
     * @return a GenerealizationResult, the result of the generalization check.
     */
    GeneralizationResult apply(BoolExpr originalFormula, CandidateInvariant candidate);
    
}
