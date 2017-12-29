package Core;

import list.Tuple;
import negotiator.parties.NegotiationInfo;
import negotiator.persistent.StandardInfoList;
import negotiator.persistent.StandardInfo;
import negotiator.persistent.PersistentDataContainer;

import java.util.HashMap;
import java.util.Map;

import static Core.ThrashAgent.gLog;

public class BidHistory {
    private StandardInfoList history;
    private NegotiationInfo info;

    public BidHistory(NegotiationInfo info, PersistentDataContainer pData) {
        this.info = info;

        switch (pData.getPersistentDataType()) {
            case DISABLED:
                break;
            case SERIALIZABLE:
                break;
            case STANDARD:
                history = (StandardInfoList) pData.get();

                if (!history.isEmpty()) {
                    // example of using the bidHistory. Compute for each party the maximum utility of the bids in last session.
                    Map<String, Double> maxUtils = new HashMap<>();
                    StandardInfo lastInfo = history.get(history.size() - 1);
                    for (Tuple<String, Double> offered : lastInfo.getUtilities()) {
                        String party = offered.get1();
                        Double util = offered.get2();
                        maxUtils.put(party, maxUtils.containsKey(party) ? Math.max(maxUtils.get(party), util) : util);
                    }
                    gLog.println(maxUtils);
                }
                break;
        }
    }

    public void analyzeHistory() {
        // from recent to older bidHistory records
        for (int h = history.size() - 1; h >= 0; h--) {

            gLog.println("History index: " + h);

            StandardInfo lastinfo = history.get(h);

            int counter = 0;
            for (Tuple<String, Double> offered : lastinfo.getUtilities()) {
                counter++;

                String party = offered.get1();  // get partyID -> example: ConcederParty@15
                Double util = offered.get2();   // get the offer utility

                gLog.println("PartyID: " + party + " utilityForMe: " + util);
                gLog.println();
                //just print first 3 bids, not the whole bidHistory
                if (counter == 3)
                    break;
            }

            gLog.println("\n");
        }

    }
}
