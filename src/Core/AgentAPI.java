package Core;

import Utils.SAparams;
import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;

import java.util.Random;

public interface AgentAPI {

    /**
     * Sets the random number generator.
     *
     * @return the RNG.
     */
    Random getRand();

    /**
     * Sets the agent strategy.
     *
     * @return the agent strategy.
     */
    StrategyEnum getAgentStrategy();

    /**
     * Sets the agent searching method.
     *
     * @return the agent searching method.
     */
    SearchMethodEnum getSearchingMethod();

    /**
     * Sets the agent frequency value selection method.
     *
     * @return the frequency value selection method.
     */
    ValFreqEnum getFrequencyValueSelection();

    /**
     * Sets the simulated annealing parameters.
     *
     * @return the SA params
     */
    SAparams getSimulatedAnnealingParams();

    /**
     * Set bid utility threshold.
     *
     * @return the utility threshold chosen by BidStrategy.
     */
    double getBidUtilThreshold();

}
