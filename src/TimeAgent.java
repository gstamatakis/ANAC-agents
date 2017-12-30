import Core.ThrashAgent;
import Utils.SearchMethodEnum;
import Utils.SimulatedAnnealingParams;
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
        return 0.99;
    }

    @Override
    public double getTimeScalingFactor() {
        return 1;
    }

    @Override
    public double getCutoffValue() {
        return 1e-6;
    }

    @Override
    public double getVetoVal() {
        return 0;
    }

    @Override
    public boolean useHistory() {
        return false;
    }

    @Override
    public double getConcessionThreshold() {
        return 0.98;
    }

    @Override
    public int getMemoryDepth() {
        return 3;
    }

    @Override
    public String getDescription() {
        return "Ti SA Std";
    }
}
