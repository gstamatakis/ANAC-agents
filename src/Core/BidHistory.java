package Core;

import list.Tuple;
import negotiator.AgentID;
import negotiator.Bid;
import negotiator.parties.NegotiationInfo;
import negotiator.persistent.StandardInfoList;
import negotiator.persistent.StandardInfo;
import negotiator.persistent.PersistentDataContainer;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static Core.ThrashAgent.MemoryDepth;
import static Core.ThrashAgent.bidHistory;
import static Core.ThrashAgent.gLog;

public class BidHistory {
    private final NegotiationInfo info;
    private StandardInfoList history;
    private Map<String, Double> bestOfferedUtils;
    private Map<String, Double> worstOfferedUtils;
    private Map<String, Map<Integer, Double>> acceptedUtils;    //The accepted bids of enemies for different rounds
    private int round;
    private Random RNG;

    public BidHistory(NegotiationInfo info, Random RNG, PersistentDataContainer pData) {
        this.RNG = RNG;
        this.info = info;

        switch (pData.getPersistentDataType()) {
            case DISABLED:
                break;
            case SERIALIZABLE:
                break;
            case STANDARD:
                this.history = (StandardInfoList) pData.get();
                this.bestOfferedUtils = new HashMap<>();
                this.worstOfferedUtils = new HashMap<>();
                this.acceptedUtils = new HashMap<>();

                //Compute for each party the maximum and minimum utility in last session.
                if (!history.isEmpty()) {
                    this.round = history.size();

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

                            acceptedUtils.put(party, new HashMap<>());
                            acceptedUtils.get(party).put(i, util);
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
        gLog.println("Accepted");
        gLog.println(acceptedUtils);
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

    public double getLuckyValue(AgentID partyId) {
        double val = 1.0;
        try {
            for (int i : acceptedUtils.get(partyId.getName()).keySet()) {
                val = Math.min(acceptedUtils.get(partyId.getName()).get(i), val);
            }
        } catch (Exception ignored) {
        }
        gLog.println("Lucky val for " + partyId.getName() + ": " + val);
        return 1.0;
    }
}
