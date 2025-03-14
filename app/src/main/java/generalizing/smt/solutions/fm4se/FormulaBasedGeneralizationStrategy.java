package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.BoolExpr;

/**
 * Interface for formula-based generalization strategies.
 * These strategies analyze formulas to find patterns, relationships,
 * and constraints between variables.
 * 
 * Each strategy should:
 * 1. Analyze the formula structure or solutions
 * 2. Identify patterns or relationships
 * 3. Return results in a structured format
 * 4. Document its performance characteristics
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public interface FormulaBasedGeneralizationStrategy {
    /**
     * Applies the strategy to a formula.
     * 
     * @param formula The formula to analyze
     * @param candidate Optional candidate invariant to verify
     * @return Results of the analysis
     */
    GeneralizationResult apply(BoolExpr formula, CandidateInvariant candidate);
}
