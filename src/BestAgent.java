import Core.ThrashAgent;
import Utils.SimulatedAnnealingParams;
import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;
import negotiator.parties.NegotiationInfo;

import java.util.Random;

public class BestAgent extends ThrashAgent {

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
    }

    @Override
    public Random getRand() {
        return new Random(42); //TODO Remove on Release;
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
        return new SimulatedAnnealingParams(1.0, 0.001, 0.999, 1);
    }

    @Override
    public double getBidUtilThreshold() {
        return 0.99;
    }

    @Override
    public String getDescription() {
        return "Th SA Std";
    }
}