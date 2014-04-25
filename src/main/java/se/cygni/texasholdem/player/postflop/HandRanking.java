package se.cygni.texasholdem.player.postflop;

public class HandRanking {

    private final Hand hand;
    private final int rankingValue;
    private final int pocketCardsInPokerHand;

    public HandRanking(Hand hand, int rankingValue, int pocketCardsInPokerHand) {
        this.hand = hand;
        this.rankingValue = rankingValue;
        this.pocketCardsInPokerHand = pocketCardsInPokerHand;
    }

    public int getRankingValue() {
        return rankingValue;
    }

    public Hand getHand() {
        return hand;
    }

    public int getPocketCardsInPokerHand() {
        return pocketCardsInPokerHand;
    }

}
