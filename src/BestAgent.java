import Core.ThrashAgent;
import Utils.SearchMethodEnum;
import Utils.SimulatedAnnealingParams;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;

import java.util.Random;

public class BestAgent extends ThrashAgent {

    @Override
    public Random getRand() {
        return new Random();
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
    public SimulatedAnnealingParams getSimulatedAnnealingParams() {
        return new SimulatedAnnealingParams(1.0, 0.001, 0.999, 2);
    }

    @Override
    public double getBidUtilThreshold() {
        return 0.96;
    }

    @Override
    public double getTimeScalingFactor() {
        return 0;
    }

    @Override
    public double getCutoffValue() {
        return 1e-6;
    }

    @Override
    public double getVetoVal() {
        return 0.00;
    }

    @Override
    public boolean useHistory() {
        return true;
    }

    @Override
    public double getSoftConcessionThreshold() {
        return 0.80;
    }

    @Override
    public double getConcessionThreshold() {
        return 0.95;
    }

    @Override
    public int getMemoryDepth() {
        return 4;
    }

    @Override
    public String getDescription() {
        return "Th SA Std";
    }
}