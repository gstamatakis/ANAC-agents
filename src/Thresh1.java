import Core.ThrashAgent;
import Utils.SAparams;
import Utils.SearchMethodEnum;
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
        return ValFreqEnum.ValueFreq;
    }

    @Override
    public SAparams getSimulatedAnnealingParams() {
        return new SAparams(1.0, 0.01, 0.999, 1);
    }

    @Override
    public double getBidUtilThreshold() {
        return 0.999;
    }

    @Override
    public String getDescription() {
        return "TAgent1";
    }
}
