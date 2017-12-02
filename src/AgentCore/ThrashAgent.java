package AgentCore;

import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.*;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.util.List;
import java.util.Random;

/**
 * Agent ThresholdAgent.
 */
public abstract class ThrashAgent extends AbstractNegotiationParty implements AgentAPI {
    static StrategyEnum AgentStrat;
    static SearchMethodEnum SearchingMethod;
    static ValFreqEnum ValueFrequencySel;

    private AbstractUtilitySpace utilitySpace;
    private NegotiationStatistics Information;
    private BidStrategy bidStrategy;
    private int supporter_num = 0;
    private Bid offeredBid = null;
    private Random RNG;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        this.RNG = getRand();

        AgentStrat = getAgentStrategy();
        SearchingMethod = getSearchingMethod();
        ValueFrequencySel = getFrequencyValueSelection();

        utilitySpace = info.getUtilitySpace();
        Information = new NegotiationStatistics(utilitySpace, RNG);
        bidStrategy = new BidStrategy(utilitySpace, Information, RNG, getBidUtilThreshold(), getSimulatedAnnealingParams());
    }

    /**
     * Each round this method gets called and ask you to accept or offer. The
     * first party in the first round is a bit different, it can only propose an
     * offer.
     *
     * @param validActions Either a list containing both accept and offer or only offer.
     * @return The chosen action.
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> validActions) {
        Information.incrementRound();
        double time = getTimeLine().getTime();
        Double targetTime = bidStrategy.targetTime(time);

        Bid bidToOffer = bidStrategy.SimulatedAnnealingUop(utilitySpace.getDomain().getRandomBid(RNG), targetTime, time);

        if (validActions.contains(Offer.class) && Information.getRound() <= 1) {
            return new Offer(getPartyId(), Information.updateMyBidHistory(bidStrategy.getMaxBid()));
        } else if (validActions.contains(Accept.class)) {
            switch (AgentStrat) {
                case Time:
                    if (utilitySpace.getUtilityWithDiscount(offeredBid, time) >= targetTime) {
                        return new Accept(getPartyId(), offeredBid);
                    }
                case Threshold:
                    if (utilitySpace.getUtility(offeredBid) >= bidStrategy.getThreshold(time)) {
                        return new Accept(getPartyId(), offeredBid);
                    }
                case Mixed:
                    if (utilitySpace.isDiscounted()) {
                        if (utilitySpace.getUtilityWithDiscount(offeredBid, time) >= targetTime) {
                            return new Accept(getPartyId(), offeredBid);
                        }
                    } else {
                        if (utilitySpace.getUtility(offeredBid) >= bidStrategy.getThreshold(time)) {
                            return new Accept(getPartyId(), offeredBid);
                        }
                    }
                default:
                    System.out.println("Unknown AgentStrat");
                    return new Offer(getPartyId(), Information.updateMyBidHistory(bidToOffer));
            }
        } else if (validActions.contains(EndNegotiation.class) && bidStrategy.selectEndNegotiation(time)) {
            return new EndNegotiation(getPartyId());
        } else {
            return new Offer(getPartyId(), Information.updateMyBidHistory(bidToOffer));
        }
    }

    /**
     * All offers proposed by the other parties will be received as a message.
     * You can use this information to your advantage, for example to predict
     * their utility.
     *
     * @param sender The party that did the action. Can be null.
     * @param action The action that party did.
     */
    @Override
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);
        double time = getTimeLine().getTime();
        if (action != null) {
            if (action instanceof Inform) {
                Integer opponentsNum = (Integer) ((Inform) action).getValue();
                Information.updateOpponentsNum(opponentsNum);
            } else if (action instanceof Offer) {
                if (!Information.getOpponents().containsKey(sender)) {
                    Information.initOpponent(sender);
                }
                supporter_num = 1;
                offeredBid = ((Offer) action).getBid();
                Information.updateInfo(sender, offeredBid, time);
            } else if (action instanceof Accept) {
                if (!Information.getOpponents().containsKey(sender)) {
                    Information.initOpponent(sender);
                }
                Information.updateAcceptanceHistory(sender, offeredBid);
                supporter_num++;
            } else if (action instanceof EndNegotiation) {
                System.out.println("GAME OVER!");
            }

            if (offeredBid != null && supporter_num == Information.getNegotiatorNum() - 1) {
                Information.updatePopularBidList(offeredBid);
            }
        }
    }
}