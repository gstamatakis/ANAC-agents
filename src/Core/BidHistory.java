package Core;

import list.Tuple;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.parties.NegotiationInfo;
import negotiator.persistent.StandardInfoList;
import negotiator.persistent.StandardInfo;
import negotiator.persistent.PersistentDataContainer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static Core.ThrashAgent.MemoryDepth;
import static Core.ThrashAgent.bidHistory;
import static Core.ThrashAgent.gLog;

public class BidHistory {
    private StandardInfoList history;
    private Map<String, Double> bestOfferedUtils;
    private Map<String, Double> worstOfferedUtils;
    private Map<String, Map<Integer, Double>> acceptedUtils;    //The accepted bids of enemies for different rounds
    private int round;
    private Random RNG;

    public BidHistory(Random RNG, PersistentDataContainer pData) {
        this.RNG = RNG;
        this.bestOfferedUtils = new HashMap<>();
        this.worstOfferedUtils = new HashMap<>();
        this.acceptedUtils = new HashMap<>();

        switch (pData.getPersistentDataType()) {
            case DISABLED:
                break;
            case SERIALIZABLE:
                break;
            case STANDARD:
                this.history = (StandardInfoList) pData.get();
                this.round = history.size();

                //Compute for each party the maximum and minimum utility in last session.
                if (!history.isEmpty()) {

                    if (MemoryDepth < 1 || history.size() <= MemoryDepth) {
                        MemoryDepth = history.size() - 1;
                    }

                    for (int i = history.size() - MemoryDepth; i < history.size(); i++) {
                        StandardInfo curInfo = history.get(i);

                        for (Tuple<String, Double> offered : curInfo.getUtilities()) {
                            String party = offered.get1().split("@")[0];
                            Double util = offered.get2();
                            bestOfferedUtils.put(party, bestOfferedUtils.containsKey(party) ? Math.max(bestOfferedUtils.get(party), util) : util);
                            worstOfferedUtils.put(party, worstOfferedUtils.containsKey(party) ? Math.min(worstOfferedUtils.get(party), util) : util);

                            if (!acceptedUtils.containsKey(party)) {
                                acceptedUtils.put(party, new HashMap<>());
                            }

                            acceptedUtils.get(party).put(i, util);
                        }
                    }

                    gLog.println("Accepted values");
                    for (String party : acceptedUtils.keySet()) {
                        for (Map.Entry<Integer, Double> value : acceptedUtils.get(party).entrySet()) {
                            gLog.println(party + " " + value);
                        }
                    }
                }
                break;
        }
    }

    public void logHistory() {
        gLog.println("Best");
        gLog.println(bestOfferedUtils);
        gLog.println("Worst");
        gLog.println(worstOfferedUtils);
    }

    public Map<String, Double> getBestOfferedUtils() {
        return this.bestOfferedUtils;
    }

    public Map<String, Double> getWorstOfferedUtils() {
        return this.worstOfferedUtils;
    }

    public void setBestOppVals(Opponent opponent, AgentID sender) {
        try {
            opponent.BestOfferUtil = bestOfferedUtils.getOrDefault(sender.getName().split("@")[0], null);
            opponent.WorstOfferUtil = worstOfferedUtils.getOrDefault(sender.getName().split("@")[0], null);
        } catch (Exception ignored) {
        }
    }

    public double getLuckyBid(AgentID partyId) {
        double l = acceptedUtils.get(partyId.getName()).get(RNG.nextInt(round));
        gLog.println("Lucky BID");
        return l;
    }
}
