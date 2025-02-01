package generalizing.smt.solutions.fm4se;

/**
 * GeneralizationResult is a class that represents the outcome of a generalization check. 
 * It stores the name of the strategy and a description of the result.
 */
public class GeneralizationResult {

    private final String strategyName;
    private final String description;

    /**
     * Constructs a GeneralizationResult with the given strategy name and description.
     * @param strategyName the name of the strategy that was used.
     * @param description the description of the result of the generalization check.
     */
    public GeneralizationResult(String strategyName, String description)
    {
        this.strategyName = strategyName;
        this.description = description;
    }

    /**
     * Returns the name of the strategy that was used.
     * @return the name of the strategy.
     */
    public String getStrategyName()
    {
        return this.strategyName;
    }

    /**
     * Returns the description of the result of the generalization check.
     * @return the description of the result.
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
        return strategyName + ": " + description;
    }
}
