package Bad;

import negotiator.AgentID;
import negotiator.Bid;
import negotiator.actions.Accept;
import negotiator.actions.Action;
import negotiator.actions.Offer;
import negotiator.bidding.BidDetails;
import negotiator.issue.Issue;
import negotiator.issue.IssueDiscrete;
import negotiator.issue.Value;
import negotiator.parties.AbstractNegotiationParty;
import negotiator.parties.NegotiationInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public abstract class BadAgent extends AbstractNegotiationParty {

    private static double MINIMUM_BID_UTILITY = 0.7;
    private static double CHILL_TIME = 0.8;

    private Bid lastReceivedBid = null;
    private AgentID lastReceivedID = null;
    private String opponentType = null;
    // A map of all opponents and their bid bidHistory. Check BadOpponent class.
    private HashMap<AgentID, BadOpponent> enemies = new HashMap<>();


    @Override
    public void init(NegotiationInfo info) {

        super.init(info);

        System.out.println("Discount Factor is " + info.getUtilitySpace().getDiscountFactor());
        System.out.println("Reservation Value is " + info.getUtilitySpace().getReservationValueUndiscounted());


    }


    public Action chooseAction(List<Class<? extends Action>> validActions) {

        Bid BestWeCanDo = this.getHighestUtilityBid();
        Bid randBid = null;
        try {
            randBid = this.getRandomBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (lastReceivedBid == null || !validActions.contains(Accept.class)) {
            // If we are first, offer our best bid.
            return new Offer(getPartyId(), BestWeCanDo);
        } else {
            // Accept any bids better than ours.
            if (getUtility(lastReceivedBid) >= getUtility(BestWeCanDo)) {
                return new Accept(getPartyId(), lastReceivedBid);
            }
            // Check if opponent's type is decided and change strategy.
            else {
                if (opponentType != null) {
                    if (opponentType.equals("boulware")) {
                        // TODO
                        return new Offer(this.getPartyId(), BestWeCanDo);
                    } else if (opponentType.equals("conceder")) {
                        // TODO
                        return new Offer(this.getPartyId(), BestWeCanDo);
                    } else if (opponentType.equals("co-op")) {
                        // TODO
                        return new Offer(this.getPartyId(), BestWeCanDo);
                    } else {
                        // TODO
                        return new Offer(this.getPartyId(), BestWeCanDo);
                    }
                }
                // When time is running out chill a bit.
                if (this.timeToChill()) {
                    // Accept any bid above a minimum threshold.
                    if (this.getUtility(lastReceivedBid) >= MINIMUM_BID_UTILITY)
                        return new Accept(getPartyId(), lastReceivedBid);

                    // Offer the last opponent's second to last bid. Just for giggles. We will do something else.
                    List<BidDetails> hist = new ArrayList<BidDetails>();

                    Bid opBestBid = null;
                    if (this.enemies.get(lastReceivedID) != null)
                        opBestBid = this.enemies.get(lastReceivedID).getBidHistory().getBestBidDetails().getBid();
                    if (this.getUtility(opBestBid) >= MINIMUM_BID_UTILITY) {
                        return new Offer(this.getPartyId(), opBestBid);
                    }

                    // If all fails, offer a random(above a certain threshold) bid.
                    return new Offer(this.getPartyId(), randBid);
                }

                return new Offer(this.getPartyId(), BestWeCanDo);
            }
        }
    }

    // Wrapper for getMaxUtilityBid, so we can handle the exception.
    private Bid getHighestUtilityBid() {
        try {
            Bid bid = utilitySpace.getMaxUtilityBid();
            return bid;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Example code that generates a random bid above a certain utility, check ExampleAgent for more.
    private Bid getRandomBid() throws Exception {
        HashMap<Integer, Value> values = new HashMap<Integer, Value>();

        List<Issue> issues = utilitySpace.getDomain().getIssues();
        Random randomnr = new Random();

        Bid bid = null;
        do {
            for (Issue lIssue : issues) {
                IssueDiscrete lIssueDiscrete = (IssueDiscrete) lIssue;
                int optionIndex = randomnr.nextInt(lIssueDiscrete.getNumberOfValues());
                values.put(lIssue.getNumber(), lIssueDiscrete.getValue(optionIndex));
            }
            bid = new Bid(utilitySpace.getDomain(), values);
        } while (getUtility(bid) < MINIMUM_BID_UTILITY);

        return bid;
    }

    // Checks whether a certain point in time(CHILL_TIME) has passed.
    private Boolean timeToChill() {
        if (this.getTimeLine().getTime() < CHILL_TIME)
            return false;
        return true;
    }

    // Receive a message from the server, set globals and add it to the opponent's bid bidHistory.
    public void receiveMessage(AgentID sender, Action action) {
        super.receiveMessage(sender, action);
        if (action instanceof Offer) {
            lastReceivedBid = ((Offer) action).getBid();
            lastReceivedID = sender;
            BidDetails lastReceivedBidDetails = new BidDetails(lastReceivedBid, getUtility(lastReceivedBid), this.getTimeLine().getTime());

            // Add the last received bid to the bidHistory of its Bad.
            if (this.enemies.get(lastReceivedID) == null)
                this.enemies.put(lastReceivedID, new BadOpponent(lastReceivedID));
            this.enemies.get(lastReceivedID).addToHistory(lastReceivedBidDetails);
        }
    }



}
