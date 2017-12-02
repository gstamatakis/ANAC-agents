import Core.ThrashAgent;
import Utils.SimulatedAnnealingParams;
import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;

import java.util.Random;

public class TimeAgent extends ThrashAgent {
    @Override
    public Random getRand() {
        return new Random();
    }

    @Override
    public StrategyEnum getAgentStrategy() {
        return StrategyEnum.Time;
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
    public SimulatedAnnealingParams getSimulatedAnnealingParams() {
        return new SimulatedAnnealingParams();
    }

    @Override
    public double getBidUtilThreshold() {
        return 0.999;
    }

    @Override
    public String getDescription() {
        return "Ti SA Std";
    }
}
