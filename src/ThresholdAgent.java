import AgentCore.ThrashAgent;
import Utils.SAparams;
import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;
import negotiator.parties.NegotiationInfo;

import java.util.Random;

public class ThresholdAgent extends ThrashAgent {

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
    public SAparams getSimulatedAnnealingParams() {
        return new SAparams(1.0, 0.001, 0.999, 1);
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