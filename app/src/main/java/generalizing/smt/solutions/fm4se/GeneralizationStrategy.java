package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.Model;

/**
 * Interface for generalization strategies.
 */
public interface GeneralizationStrategy {

    /**
     * Applies the generalization strategy to the given model.
     * @param model the model to generalize
     * @return the generalized model
     */
    GeneralizationResult apply (Model model);
    
}
