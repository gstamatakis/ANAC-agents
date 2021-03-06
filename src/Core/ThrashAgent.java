package Core;

import Utils.SearchMethodEnum;
import Utils.StrategyEnum;
import Utils.ValFreqEnum;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.Deadline;
import negotiator.actions.*;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.persistent.PersistentDataContainer;
import negotiator.timeline.TimeLineInfo;
import negotiator.utility.AbstractUtilitySpace;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public abstract class ThrashAgent extends AbstractNegotiationParty implements AgentAPI {
    private StrategyEnum AgentStrat;
    public static BidHistory bidHistory;
    static boolean useHistory;
    public static SearchMethodEnum SearchingMethod;
    public static ValFreqEnum ValueFrequencySel;
    static double CutoffVal;
    private static double VetoVal;
    static String myDescription;
    static double concessionThreshold;
    private AbstractUtilitySpace utilitySpace;
    private NegotiationStatistics Information;
    private BidStrategy bidStrategy;
    private int supporter_num = 0;
    private Bid offeredBid = null;
    private Random RNG;
    static PrintWriter gLog;
    static String filename;
    static int MemoryDepth;
    static double softConcessionThreshold;


    @Override
    public void init(AbstractUtilitySpace utilSpace, Deadline dl, TimeLineInfo tl, long randomSeed, AgentID agentId, PersistentDataContainer data) {
        super.init(utilSpace, dl, tl, randomSeed, agentId, data);
        this.RNG = getRand();
        utilitySpace = utilSpace;
        MemoryDepth = getMemoryDepth();
        concessionThreshold = getConcessionThreshold();
        softConcessionThreshold = getSoftConcessionThreshold();
        myDescription = getDescription();
        VetoVal = getVetoVal();
        SearchingMethod = getSearchingMethod();
        ValueFrequencySel = getFrequencyValueSelection();
        CutoffVal = getCutoffValue();
        AgentStrat = getAgentStrategy();
        filename = "C:/Users/gstamatakis/IdeaProjects/ANAC-agents/logs/" + AgentStrat + "_logs.txt";

        try {
            gLog = new PrintWriter(new FileWriter(filename, true));
        } catch (Exception e) {
            gLog = new PrintWriter(System.out);
        }
        gLog.println("*********\n" + Instant.now());

        useHistory = useHistory();
        if (useHistory) {
            try {
                bidHistory = new BidHistory(RNG, getData());
            } catch (Exception e) {
                gLog.println(e.toString());
                useHistory = false;
            }
        } else {
            gLog.println("Not using history.");
        }

        Information = new NegotiationStatistics(utilitySpace, RNG);
        bidStrategy = new BidStrategy(utilitySpace, Information, RNG, getBidUtilThreshold(), getSimulatedAnnealingParams(), getTimeScalingFactor());

        if (useHistory) {
            try {
                bidHistory.logHistory();
            } catch (Exception e) {
                gLog.println(e.toString());
            }
        }
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

            case Mixed:
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
            } else if (action instanceof Accept) {
                if (!Information.getOpponents().containsKey(sender)) {
                    Information.initOpponent(sender);
                }
                Information.updateAcceptanceHistory(sender, offeredBid);
                supporter_num++;
            } else if (action instanceof EndNegotiation) {
                gLog.println("Someone left..");
            }

            if (offeredBid != null && supporter_num == (Information.getNegotiatorNum() - 1)) {
                Information.updatePopularBidList(offeredBid);
            }
        }
    }

    @Override
    public HashMap<String, String> negotiationEnded(Bid acceptedBid) {
        gLog.println("\nnegotiationEnded");
        gLog.close();
        return super.negotiationEnded(acceptedBid);
    }
}