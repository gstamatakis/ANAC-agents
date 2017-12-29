package Core;

import Utils.SearchMethodEnum;
import Utils.SimulatedAnnealingParams;
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
    SimulatedAnnealingParams getSimulatedAnnealingParams();

    /**
     * Set bid utility threshold.
     *
     * @return the utility threshold chosen by BidStrategy.
     */
    double getBidUtilThreshold();

    /**
     * Scaling in TIME strategy.
     *
     * @return The scaling factor for Time strategy.
     */
    double getTimeScalingFactor();

    /**
     * Return the "relative zero". All values below this will be considered zero.
     *
     * @return Cutoff factor
     */
    double getCutoffValue();

    /**
     * Reject all bids below this value.
     * The opponent can still accept our offers though!
     *
     * @return The Veto value.
     */
    double getVetoVal();

    /**
     * Use BidHistory?
     *
     * @return True if the agent is allowed to use history.
     */
    boolean useHistory();
}
