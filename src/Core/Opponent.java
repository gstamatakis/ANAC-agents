package Core;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.issue.Issue;
import negotiator.issue.Value;
import negotiator.utility.AbstractUtilitySpace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class Opponent {
    AgentID agentID;
    ArrayList<Bid> BidHistory;
    ArrayList<Bid> AcceptHistory;
    ArrayList<Double> TimeBidHistory;//A list containing the time intervals at witch each Bid of the opponent was offered
    Double H;
    Double Average; // average
    Double Variance; // Dispersion
    Double Sum; // sum
    Double PowSum; // Sum of squares
    Double StandardDeviation; // standard deviation
    Double WorstOfferUtil; //the worst utility the opponent offered our agent
    Double BestOfferUtil; //the best utility the opponent offered our agent
    Double BestOfferUtilWithoutLastOffer; //the best utility the opponent offered our agent counting out his/hers last bid
    HashMap<Issue, HashMap<Value, Integer>> ValueFrequency; // the value frequency matrix of each issue of each negotiator
    HashMap<Issue, HashMap<Value, Double>> ValueFrequencyWeighted; // the value frequency matrix of each issue of each negotiator weighted by the WeightFunction
    HashMap<Issue, HashMap<Value, Integer>> AcceptedValueFrequency; // the value frequency matrix of each issue of each negotiator
    private Random random;

    Opponent(AgentID agentID, Random RNG) {
        this.agentID = agentID;

        this.H = 0.5;
        this.BidHistory = new ArrayList<>();
        this.TimeBidHistory = new ArrayList<>();
        this.AcceptHistory = new ArrayList<>();
        this.Average = 0.0;
        this.Variance = 0.0;
        this.Sum = 0.0;
        this.PowSum = 0.0;
        this.StandardDeviation = 0.0;

        this.WorstOfferUtil = null;
        this.BestOfferUtil = null;
        this.BestOfferUtilWithoutLastOffer = null;

        this.ValueFrequency = new HashMap<>();
        this.ValueFrequencyWeighted = new HashMap<>();
        this.AcceptedValueFrequency = new HashMap<>();
        this.random = RNG;
    }

    double updateNegotiatingInfo(Bid offeredBid, double timeOfTheOffer, AbstractUtilitySpace utilitySpace) {
        BidHistory.add(offeredBid);
        TimeBidHistory.add(timeOfTheOffer);

        double util = utilitySpace.getUtilityWithDiscount(offeredBid, 0.0);

        Sum += util;
        PowSum += Math.pow(util, 2);
        Average = Sum / BidHistory.size();
        Variance = (PowSum / BidHistory.size()) - Math.pow(Average, 2);

        if (Variance < 0) { //Clip negative values.
            Variance = 0.0;
        }
        StandardDeviation = Math.sqrt(Variance);

        if (WorstOfferUtil == null || util < WorstOfferUtil) {
            WorstOfferUtil = util;
        }

        if (BestOfferUtil == null && BestOfferUtilWithoutLastOffer == null) {
            BestOfferUtil = util;
            BestOfferUtilWithoutLastOffer = util;
        } else {
            BestOfferUtilWithoutLastOffer = BestOfferUtil;
            assert BestOfferUtil != null;
            if (util > BestOfferUtil) {
                BestOfferUtil = util;
            }
        }

        H = Average + random.nextGaussian() * StandardDeviation;
        return util;
    }

    public void updateAcceptedFrequency(Issue issue, Value value, int weight) {
        HashMap<Value, Integer> this_issue = this.AcceptedValueFrequency.get(issue);
        this_issue.put(value, this_issue.get(value) + weight);
    }

    public void updateWeightedFrequency(Issue issue, Value value, Double weight) {
        HashMap<Value, Double> this_issue = this.ValueFrequencyWeighted.get(issue);
        this_issue.put(value, this_issue.get(value) + weight);
    }

    public void updateFrequency(Issue issue, Value value, int weight) {
        HashMap<Value, Integer> this_issue = this.ValueFrequency.get(issue);
        this_issue.put(value, this_issue.get(value) + weight);
    }


    @Override
    public String toString() {
        return this.agentID + " " + this.BidHistory.toString();
    }
}
