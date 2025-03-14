package generalizing.smt.solutions.fm4se.strategies.bool;

import com.microsoft.z3.BoolExpr;
import java.util.Set;

/**
 * Interface for boolean analysis strategies.
 * Each strategy should analyze boolean formulas to find:
 * 1. Fixed values (variables that are always true/false)
 * 2. Implications between variables
 */
public interface BooleanStrategy {
    /**
     * Analyzes a boolean formula to find patterns in relationships between variables.
     * @param formula The formula to analyze
     * @param variables The set of boolean variables to analyze
     * @return A string containing the analysis results
     */
    String analyzeRelations(BoolExpr formula, Set<String> variables);
} 