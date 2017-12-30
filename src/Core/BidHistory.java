package Core;

import list.Tuple;
import negotiator.AgentID;
import negotiator.parties.NegotiationInfo;
import negotiator.persistent.StandardInfoList;
import negotiator.persistent.StandardInfo;
import negotiator.persistent.PersistentDataContainer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static Core.ThrashAgent.MemoryDepth;
import static Core.ThrashAgent.bidHistory;
import static Core.ThrashAgent.gLog;

public class BidHistory {
    private StandardInfoList history;
    private NegotiationInfo info;
    private Map<String, Double> bestOfferedUtils;
    private Map<String, Double> worstOfferedUtils;

    public BidHistory(NegotiationInfo info, PersistentDataContainer pData) {
        this.info = info;

        switch (pData.getPersistentDataType()) {
            case DISABLED:
                break;
            case SERIALIZABLE:
                break;
            case STANDARD:
                history = (StandardInfoList) pData.get();

                //Compute for each party the maximum and minimum utility in last session.
                if (!history.isEmpty() && MemoryDepth > 0 && MemoryDepth <= history.size()) {
                    bestOfferedUtils = new HashMap<>();
                    worstOfferedUtils = new HashMap<>();

                    for (int i = history.size() - 1; i >= history.size() - MemoryDepth; i--) {
                        StandardInfo lastInfo = history.get(history.size() - 1);

                        for (Tuple<String, Double> offered : lastInfo.getUtilities()) {
                            String party = offered.get1().split("@")[0];
                            Double util = offered.get2();
                            bestOfferedUtils.put(party, bestOfferedUtils.containsKey(party) ? Math.max(bestOfferedUtils.get(party), util) : util);
                            worstOfferedUtils.put(party, worstOfferedUtils.containsKey(party) ? Math.min(worstOfferedUtils.get(party), util) : util);
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

    public void initOppVals(Opponent opponent, AgentID sender) {
        try {
            opponent.BestOfferUtil = bestOfferedUtils.getOrDefault(sender.getName().split("@")[0], null);
            opponent.WorstOfferUtil = worstOfferedUtils.getOrDefault(sender.getName().split("@")[0], null);
        } catch (Exception ignored) {
        }
    }
}
