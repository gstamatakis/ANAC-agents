package Core;

import Utils.SimulatedAnnealingParams;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.utility.AbstractUtilitySpace;
import negotiator.utility.AdditiveUtilitySpace;

import java.util.*;

import static Core.ThrashAgent.*;

public class BidStrategy {

    private final double scalingFactor;
    private SimulatedAnnealingParams SAparams;
    private AbstractUtilitySpace utilitySpace;
    private NegotiationStatistics Information;
    private Bid maxBid;
    private Random RNG;
    private double reservationVal;
    private double discountFactor;

    /**
     * Initialises utility space related vars and finds the initial max bid.
     * <p>
     * Example of utility space:
     * An additive utility function is characteristic of independent goods. For example, an apple and a hat are
     * considered independent: the utility a person receives from having an apple is the same whether or not he has a
     * hat, and vice versa.
     *
     * @param utilSpace       The given utility space.
     * @param negotiatingInfo The negotiation statistics obj.
     */
    public BidStrategy(AbstractUtilitySpace utilSpace, NegotiationStatistics negotiatingInfo, Random RNG,
                       double bidUtilThreshold, SimulatedAnnealingParams params, double scalingFactor) {
        this.utilitySpace = utilSpace;
        this.Information = negotiatingInfo;
        this.RNG = RNG;
        this.reservationVal = utilSpace.getReservationValueUndiscounted();
        this.discountFactor = utilSpace.getDiscountFactor();
        this.SAparams = params;
        this.scalingFactor = scalingFactor;

        // initial search of maximum utility value Bid
        if (utilitySpace instanceof AdditiveUtilitySpace) {
            try {
                maxBid = utilitySpace.getMaxUtilityBid();
            } catch (Exception e) {
                gLog.println(e.toString());
            }
            Information.initValueRelativeUtility();
            return;
        }

        int tryNum = utilitySpace.getDomain().getIssues().size();
        maxBid = utilitySpace.getDomain().getRandomBid(RNG);
        for (int i = 0; i < tryNum; i++) {
            maxBid = AppropriateSearch(maxBid, -1);
            while (utilitySpace.getUtilityWithDiscount(maxBid, 0.0) < utilitySpace.getReservationValue()) {
                maxBid = AppropriateSearch(maxBid, -1);
            }
            if (utilitySpace.getUtilityWithDiscount(maxBid, 0.0) >= bidUtilThreshold) {
                break;
            }
        }
        //initialize the relative utility list
        Information.initValueRelativeUtility();
    }

    /**
     * (Eq.13.5)
     * <p>
     * Our agent proposes a bid whose
     * utility exceeds target (t) and the highest Uop (Eq. 13.4). It accepts the opponent’s
     * bids when they are more than target (t).
     * <p>
     *
     * @param time The t input.
     * @return The targetEnd that will decide the next action.
     */
    private double targetEnd(double time) {
        double weightedAverage = utilitySpace.getUtilityWithDiscount(maxBid, time);
        double normalizer = 1.0;

        if (useHistory) {
            try {
                Map<String, Double> bests = bidHistory.getBestOfferedUtils();
                for (String val : bests.keySet()) {
                    weightedAverage += bests.get(val);
                }
                return weightedAverage;
            } catch (Exception ignored) {
            }
        }

        for (AgentID ids : Information.getOpponents().keySet()) {
            HashMap<Issue, Value> bestVals = Information.getMaxValuesForOpponent(ids);
            Bid opponentBid = maxBid;
            Bid temp = maxBid;
            for (Issue issue : Information.getIssues()) {
                opponentBid = temp.putValue(issue.getNumber(), bestVals.get(issue));
                temp = opponentBid;
            }
            //Nominator
            weightedAverage += Information.getOpponents().get(ids).H * utilitySpace.getUtilityWithDiscount(opponentBid, time);
            //Denominator
            normalizer += Information.getOpponents().get(ids).H;
        }
        return weightedAverage / normalizer;
    }

    /**
     * (Eq.13.6)
     * <p>
     * Our agent proposes a bid whose
     * utility exceeds target (t) and the highest Uop (Eq. 13.4). It accepts the opponent’s
     * bids when they are more than target (t). Scaling factor is used experimentally.
     * <p>
     * target(t)
     * = (1 − t^3 )(1 − targetend ) + targetend (d=1)
     * = (1 − t^d )(1 − targetend ) + targetend (otherwise)
     *
     * @param time The t input.
     * @return The minimum utility of an offer the agent will accept.
     */
    double targetTime(double time) {
        double targetEnd = targetEnd(time);

        return scalingFactor * discountFactor != 1.0 ?
                (1 - Math.pow(time, discountFactor)) * (1 - targetEnd) + targetEnd :
                (1 - Math.pow(time, 3)) * (1 - targetEnd) + targetEnd;
    }

    /**
     * @param bid  The bid.
     * @param time The time.
     * @return The uop.
     */
    private double Uop(Bid bid, double time) {
        double temp = utilitySpace.getUtilityWithDiscount(bid, time) / utilitySpace.getUtilityWithDiscount(maxBid, time);
        double norm = 1.0;

        for (AgentID ids : Information.getOpponents().keySet()) {
            HashMap<Issue, Value> bestValues = Information.getMaxValuesForOpponent(ids);
            Bid opponentBid = null;

            Bid tempv = new Bid(maxBid);
            for (Issue issue : Information.getIssues()) {
                opponentBid = tempv.putValue(issue.getNumber(), bestValues.get(issue));
                tempv = opponentBid;
            }

            double ua = 0.0;
            double max_ua = 0.0;
            HashMap<Issue, HashMap<Value, Integer>> frequencyMap = Information.getOpponents().get(ids).ValueFrequency;
            for (Issue issue : Information.getIssues()) {
                Value value1 = bid.getValue(issue.getNumber());
                ua += frequencyMap.get(issue).get(value1);
                if (opponentBid != null) {
                    max_ua += frequencyMap.get(issue).get(opponentBid.getValue(issue.getNumber()));
                }
            }

            norm += Information.getOpponents().get(ids).H;
            temp += Information.getOpponents().get(ids).H * ua / max_ua;
        }

        return temp / norm;
    }

    /**
     * Performs Simulated annealing (SimulatedAnnealing), a probabilistic technique for approximating the global optimum of a given function.
     * Start by looping until the temperature drops bellow a specific threshold.
     * Continue by calculating the closest optimal bid while making random steps.
     * Calculate the cost of moving to a next state.
     *
     * @param threshold The threshold.
     * @param time      The time remaining.
     * @param baseBid   The initial position.
     */
    private ArrayList<Bid> SimulatedAnnealing(double threshold, double time, Bid baseBid) {
        ArrayList<Bid> targetBids = new ArrayList<>(); //optimal utility
        double currentBidUtil = utilitySpace.getUtilityWithDiscount(baseBid, 0);
        double newEnergy, curEnergy, p;
        double curTemp = SAparams.getStartTemperature();
        double targetBidUtil = 0.0;
        double nextBidUtil = 0.0;
        Bid nextBid = null;
        List<Issue> issues = Information.getIssues();

        while (curTemp > SAparams.getEndTemperature()) {
            for (int i = 0; i < SAparams.getStepNum(); i++) {
                Issue issue = issues.get(RNG.nextInt(issues.size()));
                ArrayList<Value> values = Information.getValues(issue);
                int valueIndex = RNG.nextInt(values.size());
                assert baseBid != null;
                nextBid = baseBid.putValue(issue.getNumber(), values.get(valueIndex));
                nextBidUtil = utilitySpace.getUtilityWithDiscount(nextBid, time);
                if (maxBid == null || nextBidUtil >= utilitySpace.getUtilityWithDiscount(maxBid, time)) {
                    maxBid = nextBid;
                }
            }

            newEnergy = Math.abs(threshold - nextBidUtil);
            curEnergy = Math.abs(threshold - currentBidUtil);
            p = Math.exp(-Math.abs(newEnergy - curEnergy) / curTemp);

            if (newEnergy < curEnergy || p > RNG.nextDouble()) {
                baseBid = nextBid;
                currentBidUtil = nextBidUtil;
            }

            if (currentBidUtil >= threshold) {
                if (!targetBids.isEmpty()) {
                    if (currentBidUtil < targetBidUtil) {
                        targetBids.clear();
                        targetBids.add(baseBid);
                        targetBidUtil = currentBidUtil;
                    } else if (currentBidUtil == targetBidUtil) {
                        targetBids.add(baseBid);
                    }
                } else {
                    targetBids.add(baseBid);
                    targetBidUtil = currentBidUtil;
                }
            }

            curTemp = curTemp * SAparams.getCool();
        }
        return targetBids;
    }

    /**
     * When it can not find the Bid with a large utility value than the boundary value, return the baseBid
     * utility value returns a Bid which is near the boundary value
     *
     * @param baseBid The base Bid.
     */
    private Bid SimulatedAnnealingSearch(Bid baseBid, double threshold, double time) {
        ArrayList<Bid> targetBids = SimulatedAnnealing(threshold, time, baseBid);
        return targetBids.isEmpty() ? baseBid : targetBids.get(RNG.nextInt(targetBids.size()));
    }

    /**
     * Simulated Annealing for UOP
     *
     * @param baseBid   The base Bid.
     * @param threshold The Threshold.
     * @param time      The time.
     * @return The selected Bid.
     */
    Bid SimulatedAnnealingUop(Bid baseBid, double threshold, double time) {
        ArrayList<Bid> targetBids = SimulatedAnnealing(threshold, time, baseBid);

        if (targetBids.isEmpty()) {
            return baseBid;
        }

        // utility value returns a Bid which is near the boundary value
        double maxUopUtil = Double.MIN_VALUE;
        Bid max_bid = null;
        for (Bid bids : targetBids) {
            double uoptmp = Uop(bids, time);
            if (uoptmp > maxUopUtil) {
                maxUopUtil = uoptmp;
                max_bid = bids;
            }
        }

        return max_bid != null ? max_bid : targetBids.get(RNG.nextInt(targetBids.size() - 1));
    }

    public Bid AppropriateSearch(Bid val, double time) {
        switch (ThrashAgent.SearchingMethod) {
            case SimulatedAnnealing:
                return time == -1 ?
                        SimulatedAnnealingSearch(val, 1.0, 0) :
                        SimulatedAnnealingSearch(val, getThreshold(time), time);
            case Relative:
                return relativeUtilitySearch(val);
            default:
                throw new IllegalStateException("Default case at AppropriateSearch");
        }
    }

    private Bid relativeUtilitySearch(Bid thresholdBid) {
        Bid bid = new Bid(maxBid);
        double d = utilitySpace.getUtility(thresholdBid) - 1.0; // Difference from maximum utility value
        double concessionSum = 0.0; // Sum of reduced utility values
        double relativeUtility;
        ArrayList<Issue> randomIssues = (ArrayList<Issue>) Information.getIssues();
        Collections.shuffle(randomIssues);

        // For each issue being shuffled
        for (Issue issue : randomIssues) {
            ArrayList<Value> randomValues = Information.getValues(issue);
            Collections.shuffle(randomValues);

            // For each shuffled option
            for (Value value : randomValues) {
                relativeUtility = Information.valueRelativeUtility.get(issue).get(value); // Relative utility value based on maximum utility value
                if (d <= concessionSum + relativeUtility) {
                    bid = bid.putValue(issue.getNumber(), value);
                    concessionSum += relativeUtility;
                    break;
                }
            }
        }
        return bid;
    }

    /**
     * Used for threshold in strategy.
     *
     * @param time Current time.
     * @return The allowed threshold based on the given time.
     */
    double getThreshold(double time) {
        double threshold = 1.0;
        double emax = 1.0;

        //Calculate emax for all negotiating partners and search for the smallest one
        HashMap<AgentID, Opponent> rivals = Information.getOpponents();

        for (AgentID sender : rivals.keySet()) {
            double avg = rivals.get(sender).Average;
            double sd = rivals.get(sender).StandardDeviation;
            double e2 = Math.max(rivals.get(sender).BestOfferUtil, avg + (1 - avg) * calWidth(avg, sd));

            // emax ~= Math.min(emax, m + (1 - m) * calWidth(m, sd));
            emax = Math.max(Math.min(emax, e2), reservationVal);
        }

        threshold = 1.0 - discountFactor < CutoffVal ?
                Math.min(threshold, 1 - (1 - emax) * Math.pow(time, 3.0)) :
                Math.max(threshold - time, emax);

        // make a concession ASAP
        if (time > concessionThreshold) {
            for (AgentID sender : rivals.keySet()) {
                threshold = Math.min(threshold, rivals.get(sender).BestOfferUtil);
            }
            threshold = Math.max(threshold, reservationVal);
        } else if (useHistory && time > softConcessionThreshold) {
            for (AgentID sender : rivals.keySet()) {
                threshold = Math.min(threshold, bidHistory.getLuckyValue(sender));
            }
        }

        return threshold;
    }

    /**
     * Estimate displacement width of opponent based on statistical information (utilize bisectional uniform distribution)
     *
     * @param avg Average utility value
     * @param sd  standard deviation
     * @return Estimated displacement width of opponent
     */
    private double calWidth(double avg, double sd) {
        return avg > 0.1 && avg < 0.9 ? Math.sqrt(3.0 / (avg - avg * avg)) * sd : Math.sqrt(12) * sd;
    }

    /**
     * End negotiations when discount factor is too small.
     *
     * @param time The current time.
     * @return True if negotiations need to end.
     */
    boolean selectEndNegotiation(double time) {
        return 1.0 - discountFactor > CutoffVal && reservationVal > getThreshold(time);
    }

    public Bid getMaxBid() {
        return maxBid;
    }
}