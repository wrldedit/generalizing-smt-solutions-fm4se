package generalizing.smt.solutions.fm4se;

/**
 * Represents the result of a generalization strategy analysis.
 * This class encapsulates:
 * 1. The name of the strategy used
 * 2. A detailed description of the findings
 * 3. Optional performance metrics
 * 
 * The description should be formatted for readability and
 * include all relevant information about patterns, relationships,
 * or constraints found during analysis.
 * 
 * @author Fritz Trede
 * @version 1.0
 */
public class GeneralizationResult {

    private final String strategyName;
    private final String description;

    /**
     * Creates a new generalization result.
     * 
     * @param strategyName Name of the strategy that produced this result
     * @param description Detailed description of the findings
     */
    public GeneralizationResult(String strategyName, String description)
    {
        this.strategyName = strategyName;
        this.description = description;
    }

    /**
     * Gets the name of the strategy that produced this result.
     * 
     * @return The strategy name
     */
    public String getStrategyName()
    {
        return this.strategyName;
    }

    /**
     * Gets the detailed description of the findings.
     * 
     * @return The description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns a string representation of the GeneralizationResult.
     * @return a string representation of the GeneralizationResult.
     */
    @Override
    public String toString()
    {
        return String.format("Strategy: %s\n%s", strategyName, description);
    }
}
