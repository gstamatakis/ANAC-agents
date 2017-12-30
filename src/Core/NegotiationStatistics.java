package Core;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.*;
import negotiator.utility.AbstractUtilitySpace;

import java.util.*;

import static Core.ThrashAgent.*;

/**
 * Class used to store info regarding the opponents, the utilSpace and our BidHistory.
 */
public class NegotiationStatistics {

    private AbstractUtilitySpace utilitySpace;
    private List<Issue> issues;
    private Random RNG;
    private HashMap<AgentID, Opponent> opponents;
    private ArrayList<Bid> BestPopularBids;                               // List of Best Popular Bids in the negotiation
    HashMap<Issue, HashMap<Value, Double>> valueRelativeUtility;          // own relative utility value matrix of each issue value in the utility space (for linear utility space)
    private HashMap<Issue, HashMap<Value, Integer>> allValueFrequency;    // the value frequency matrix of each issue
    private double BestOfferedUtility;
    private int negotiatorNum;
    private int round;
    private double MaxPopularBidUtility;
    private double OwnWorstOfferedUtility;

    NegotiationStatistics(AbstractUtilitySpace utilitySpace, Random RNG) {
        this.utilitySpace = utilitySpace;
        issues = utilitySpace.getDomain().getIssues();
        this.RNG = RNG;
        opponents = new HashMap<>();
        BestPopularBids = new ArrayList<>();
        valueRelativeUtility = new HashMap<>();
        allValueFrequency = new HashMap<>();

        BestOfferedUtility = 0.0;
        round = 0;
        MaxPopularBidUtility = 0.0;
        OwnWorstOfferedUtility = 1.0;

        ArrayList<Value> values;
        for (Issue issue : issues) {
            allValueFrequency.put(issue, new HashMap<>());
            values = getValues(issue);
            for (Value value : values) {
                allValueFrequency.get(issue).put(value, 0);
            }
        }

        initValueRelativeUtility();
    }

    /**
     * initialization of relative utility matrix
     */
    void initValueRelativeUtility() {
        ArrayList<Value> values;
        for (Issue issue : issues) {
            valueRelativeUtility.put(issue, new HashMap<>()); // initialization of the issue row
            values = getValues(issue);
            for (Value value : values) { // Initialization of the elements of the issue row
                valueRelativeUtility.get(issue).put(value, 0.0);
            }
        }
    }

    /**
     * It returns a list of possible values ​​in issue
     *
     * @param issue The given issue. Can be of any type but Objective.
     * @return An ArrayList of values.
     */
    ArrayList<Value> getValues(Issue issue) {
        ArrayList<Value> values = new ArrayList<>();
        switch (issue.getType()) {
            case UNKNOWN:
                break;
            case DISCRETE:
                List<ValueDiscrete> valuesDis = ((IssueDiscrete) issue).getValues();
                values.addAll(valuesDis);
                break;
            case INTEGER:
                int min_value = ((IssueInteger) issue).getLowerBound();
                int max_value = ((IssueInteger) issue).getUpperBound();
                for (int j = min_value; j <= max_value; j++) {
                    values.add(new ValueInteger(j));
                }
                break;
            case REAL:
                double min = ((IssueReal) issue).getLowerBound();
                double max = ((IssueReal) issue).getUpperBound();
                for (double j = min; j <= max; j++) {
                    values.add(new ValueReal(j));
                }
                break;
            case OBJECTIVE:
                break;
            default:
                System.out.println(" Failed to get the values.");
        }
        return values;
    }

    /**
     * Opponent init
     *
     * @param sender The AgentID of the sender.
     */
    void initOpponent(AgentID sender) {
        Opponent opponent = new Opponent(RNG);
        opponent.name = sender.getName().split("@")[0];

        for (Issue issue : issues) {
            opponent.ValueFrequency.put(issue, new HashMap<>());
            opponent.ValueFrequencyWeighted.put(issue, new HashMap<>());
            opponent.AcceptedValueFrequency.put(issue, new HashMap<>());
            ArrayList<Value> values = getValues(issue);
            for (Value value : values) {
                opponent.ValueFrequency.get(issue).put(value, 0);
                opponent.ValueFrequencyWeighted.get(issue).put(value, 0.0);
                opponent.AcceptedValueFrequency.get(issue).put(value, 0);
            }
        }
        //Actual constructor call
        opponents.put(sender, opponent);
    }


    /**
     * Increments round.
     **/
    void incrementRound() {
        round++;
    }

    /**
     * Updates the negotiator number.
     *
     * @param num The new value.
     */
    void updateOpponentsNum(int num) {
        negotiatorNum = num;
    }

    /**
     * Update of own proposal information.
     *
     * @param offerBid My offered Bid.
     * @return My offered Bid.
     */
    Bid updateMyBidHistory(Bid offerBid) {
        if (utilitySpace.getUtilityWithDiscount(offerBid, 0.0) < OwnWorstOfferedUtility) { //update our agents worst offered Bid
            OwnWorstOfferedUtility = utilitySpace.getUtilityWithDiscount(offerBid, 0.0);
        }
        return offerBid;
    }

    /**
     * Update of negotiations information.
     * Update of the sender of the frequency matrix
     *
     * @param sender         The sender
     * @param offeredBid     The offered bid
     * @param timeOfTheOffer The time of the offer
     */
    void updateInfo(AgentID sender, Bid offeredBid, double timeOfTheOffer) {
        Opponent opponent = opponents.get(sender);
        double util = opponent.updateNegotiatingInfo(offeredBid, timeOfTheOffer, utilitySpace);

        if (util > BestOfferedUtility) {
            BestOfferedUtility = util;
        }

        for (Issue issue : issues) {
            Value value = offeredBid.getValue(issue.getNumber());
            opponent.updateFrequency(issue, value, 1);

            allValueFrequency.get(issue).put(value, allValueFrequency.get(issue).get(value) + 1); // update the list
        }

        opponent.Sum += util;
        opponent.PowSum += Math.pow(util, 2);

        opponent.Variance = opponent.PowSum / round;
        opponent.StandardDeviation = Math.sqrt(opponent.Variance);

        if (opponent.BestOfferUtil < util) {
            opponent.BestOfferUtil = util;
        }
        if (opponent.WorstOfferUtil > util) {
            opponent.WorstOfferUtil = util;
        }

        opponents.put(sender, opponent); //Re-insert
    }

    void updateAcceptanceHistory(AgentID sender, Bid AcceptedBid) {
        opponents.get(sender).AcceptHistory.add(AcceptedBid);
        for (Issue issue : issues) {
            opponents.get(sender).updateAcceptedFrequency(issue, AcceptedBid.getValue(issue.getNumber()), 1);
        }
    }

    void updatePopularBidList(Bid popularBid) {
        if (!BestPopularBids.contains(popularBid)) {
            BestPopularBids.add(popularBid);
            MaxPopularBidUtility = Math.max(MaxPopularBidUtility, utilitySpace.getUtilityWithDiscount(popularBid, 0.0));
            BestPopularBids.sort(new UtilityComparator());
        }
    }

    public class UtilityComparator implements Comparator<Bid> {
        public int compare(Bid a, Bid b) {
            return Double.compare(
                    utilitySpace.getUtilityWithDiscount(a, 0.0),
                    utilitySpace.getUtilityWithDiscount(b, 0.0)
            );
        }
    }

    int getNegotiatorNum() {
        return negotiatorNum;
    }

    int getRound() {
        return round;
    }

    List<Issue> getIssues() {
        return issues;
    }

    HashMap<AgentID, Opponent> getOpponents() {
        return opponents;
    }

    /**
     * Based on the frequency of opponentsValueFrequency and opponentsAcceptedValueFrequency return the
     * most frequent value of this issue of this opponent
     *
     * @param sender The AgentID of the sender.
     * @param issue  The issue.
     * @return The most frequent value of this issue of this opponent
     */
    private Value getValueFrequencyList(AgentID sender, Issue issue) {
        int curFreq;
        int maxFreq = 0;
        Value max_value = null;
        ArrayList<Value> randomOrderValues = getValues(issue);

        // sort in random
        Collections.shuffle(randomOrderValues);

        for (Value value : randomOrderValues) {
            switch (ThrashAgent.ValueFrequencySel) {
                case ValueFreq:
                    curFreq = opponents.get(sender).ValueFrequency.get(issue).get(value);
                    break;
                case AcceptedValueFreq:
                    curFreq = opponents.get(sender).AcceptedValueFrequency.get(issue).get(value);
                    break;
                default:
                    throw new IllegalStateException("Default case of ValueFrequencySel");
            }

            // Record the most frequent element
            if (max_value == null || curFreq > maxFreq) {
                maxFreq = curFreq;
                max_value = value;
            }
        }

        return max_value;
    }

    HashMap<Issue, Value> getMaxValuesForOpponent(AgentID opponent) {
        HashMap<Issue, Value> bestValuesForOpponent = new HashMap<>();
        for (Issue issue : issues) {
            bestValuesForOpponent.put(issue, getValueFrequencyList(opponent, issue));
        }
        return bestValuesForOpponent;
    }

}