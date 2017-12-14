package Core;

import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.*;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Agent BestAgent.
 */
public abstract class ThrashAgent extends AbstractNegotiationParty implements AgentAPI {
    private StrategyEnum AgentStrat;
    public static SearchMethodEnum SearchingMethod;
    public static ValFreqEnum ValueFrequencySel;
    static double CutoffVal;
    private static double VetoVal;

    private AbstractUtilitySpace utilitySpace;
    private NegotiationStatistics Information;
    private BidStrategy bidStrategy;
    private int supporter_num = 0;
    private Bid offeredBid = null;
    private Random RNG;
    private HashMap<Integer, Bid> lastBids;

    static PrintWriter gLog;

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);
        this.RNG = getRand();

        AgentStrat = getAgentStrategy();
        SearchingMethod = getSearchingMethod();
        ValueFrequencySel = getFrequencyValueSelection();
        CutoffVal = getCutoffValue();
        VetoVal = getVetoVal();
        lastBids = new HashMap<>();

        try {
            //BidHistory bidHistory = new BidHistory(getData()); //TODO fix this
        } catch (Exception e) {
            gLog.println(e.toString());
        }

        utilitySpace = info.getUtilitySpace();
        Information = new NegotiationStatistics(utilitySpace, RNG);
        bidStrategy = new BidStrategy(utilitySpace, Information, RNG, getBidUtilThreshold(), getSimulatedAnnealingParams(), getTimeScalingFactor());

        try {
            gLog = new PrintWriter(new FileWriter("C:\\Users\\gstamatakis\\IdeaProjects\\ANACagents\\logs\\" + AgentStrat + "logs.txt"), true);
        } catch (Exception e) {
            gLog = new PrintWriter(System.out);
        }
        gLog.println(Instant.now());
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
        Double targetTime;
        Bid bidToOffer;

        if (Information.getRound() < 2) {
            try {
                return new Offer(getPartyId(), Information.updateMyBidHistory(utilitySpace.getMaxUtilityBid()));
            } catch (Exception e) {
                gLog.println(e.toString());
            }
        }

        switch (AgentStrat) {
            case Time:
                targetTime = bidStrategy.targetTime(time);
                bidToOffer = bidStrategy.SimulatedAnnealingUop(utilitySpace.getDomain().getRandomBid(RNG), targetTime, time);
                if (validActions.contains(Offer.class) && Information.getRound() <= 1) {
                    return new Offer(getPartyId(), Information.updateMyBidHistory(bidStrategy.getMaxBid()));
                } else if (validActions.contains(Accept.class) && utilitySpace.getUtility(bidToOffer) > VetoVal) {
                    if (utilitySpace.getUtilityWithDiscount(offeredBid, time) >= targetTime) {
                        return new Accept(getPartyId(), offeredBid);
                    }
                } else if (validActions.contains(EndNegotiation.class) && bidStrategy.selectEndNegotiation(time)) {
                    return new EndNegotiation(getPartyId());
                }
                return new Offer(getPartyId(), Information.updateMyBidHistory(bidToOffer));

            case Threshold:
                if (utilitySpace.getUtility(offeredBid) >= bidStrategy.getThreshold(time)) {
                    return new Accept(getPartyId(), offeredBid);
                } else if (validActions.contains(EndNegotiation.class) && bidStrategy.selectEndNegotiation(time)) {
                    return new EndNegotiation(getPartyId());
                }

                bidToOffer = bidStrategy.AppropriateSearch(generateRandomBid(), time);
                return new Offer(getPartyId(), Information.updateMyBidHistory(bidToOffer));

            case Mixed: //Does not EndNegotiations
                if (utilitySpace.isDiscounted()) {
                    targetTime = bidStrategy.targetTime(time);
                    bidToOffer = bidStrategy.SimulatedAnnealingUop(utilitySpace.getDomain().getRandomBid(RNG), targetTime, time);
                    if (utilitySpace.getUtilityWithDiscount(offeredBid, time) >= targetTime) {
                        return new Accept(getPartyId(), offeredBid);
                    }
                } else {
                    bidToOffer = bidStrategy.AppropriateSearch(generateRandomBid(), time);
                    if (utilitySpace.getUtility(offeredBid) >= bidStrategy.getThreshold(time)) {
                        return new Accept(getPartyId(), offeredBid);
                    } else if (validActions.contains(EndNegotiation.class) && bidStrategy.selectEndNegotiation(time)) {
                        return new EndNegotiation(getPartyId());
                    }
                }
                return new Offer(getPartyId(), Information.updateMyBidHistory(bidToOffer));

            default:
                return new Offer(getPartyId(), generateRandomBid());
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
                lastBids.put(Information.getRound() % 10, offeredBid);
            } else if (action instanceof Accept) {
                if (!Information.getOpponents().containsKey(sender)) {
                    Information.initOpponent(sender);
                }
                Information.updateAcceptanceHistory(sender, offeredBid);
                supporter_num++;
            } else if (action instanceof EndNegotiation) {
                gLog.println("Someone left..");
            }

            if (offeredBid != null && supporter_num == Information.getNegotiatorNum() - 1) {
                Information.updatePopularBidList(offeredBid);
            }
        }
    }

    @Override
    public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
        gLog.println("GGWP");
        gLog.close();
        return super.negotiationEnded(acceptedBid);
    }
}