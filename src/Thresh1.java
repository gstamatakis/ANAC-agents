import Core.ThrashAgent;
import Utils.SearchMethodEnum;
import Utils.SimulatedAnnealingParams;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;
import negotiator.parties.NegotiationInfo;

import java.util.Random;

public class Thresh1 extends ThrashAgent {

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
    }

    @Override
    public Random getRand() {
        return new Random(); //TODO Remove on Release;
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
        return 0.99;
    }

    @Override
    public double getTimeScalingFactor() {
        return 0;
    }

    @Override
    public double getCutoffValue() {
        return 1e-4;
    }

    @Override
    public double getVetoVal() {
        return 0.33;
    }

    @Override
    public String getDescription() {
        return "Th SA Std";
    }
}