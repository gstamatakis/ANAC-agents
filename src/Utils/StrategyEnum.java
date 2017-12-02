package Utils;

/**
 * The AgentStrat used by the agent to accept offers.
 */
public enum StrategyEnum {
    /**
     * Time strategy.
     */
    Time,

    /**
     * Threshold strategy.
     */
    Threshold,

    /**
     * Chose Time on discounted utility spaces and Threshold on others.
     */
    Mixed
}
