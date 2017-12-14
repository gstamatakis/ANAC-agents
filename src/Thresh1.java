import Core.ThrashAgent;
import Utils.SearchMethodEnum;
import Utils.SimulatedAnnealingParams;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;

import java.util.Random;

public class Thresh1 extends ThrashAgent {
    @Override
    public Random getRand() {
        return new Random(42);
    }

    @Override
    public StrategyEnum getAgentStrategy() {
        return StrategyEnum.Threshold;
    }

    @Override
    public SearchMethodEnum getSearchingMethod() {
        return SearchMethodEnum.SimulatedAnnealing;
    }

    @Override
    public ValFreqEnum getFrequencyValueSelection() {
        return ValFreqEnum.AcceptedValueFreq;
    }

    @Override
    public SimulatedAnnealingParams getSimulatedAnnealingParams() {
        return new SimulatedAnnealingParams(1.0, 0.001, 0.999, 1);
    }

    @Override
    public double getBidUtilThreshold() {
        return 0.999;
    }

    @Override
    public double getTimeScalingFactor() {
        return 0;
    }

    @Override
    public double getCutoffValue() {
        return 1e-7;
    }

    @Override
    public double getVetoVal() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "TAgent1";
    }
}
