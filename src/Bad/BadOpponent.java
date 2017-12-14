package Bad;

import negotiator.AgentID;
import negotiator.BidHistory;
import negotiator.bidding.BidDetails;

public class BadOpponent {
    public AgentID opponentID;
    public BidHistory bidHistory = new BidHistory();

    // Constructor.
    public BadOpponent(AgentID id) {
        this.opponentID = id;
    }

    // Get the opponent's bid history.
    public BidHistory getBidHistory() {
        return this.bidHistory;
    }

    // Add a bid to the opponent's bid history.
    public void addToHistory(BidDetails bid) {
        this.bidHistory.add(bid);
    }

}
