package generalizing.smt.solutions.fm4se.strategies.integer;

import com.microsoft.z3.*;
import java.util.Set;

/**
 * Interface for strategies that analyze integer variables in formulas
 */
public interface IntegerStrategy {
    /**
     * Analyzes the formula to find patterns in integer variables
     * @param formula The formula to analyze
     * @param variables The set of variable names to analyze
     * @return A string containing the analysis results
     */
    String analyzeIntegers(BoolExpr formula, Set<String> variables);
} 