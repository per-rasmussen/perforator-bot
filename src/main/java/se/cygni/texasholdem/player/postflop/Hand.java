package se.cygni.texasholdem.player.postflop;

import se.cygni.texasholdem.game.Card;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

public class Hand implements Comparable<Hand> {

    private final se.cygni.texasholdem.game.Hand hand;

    public Hand(se.cygni.texasholdem.game.Hand hand) {
        this.hand = hand;
    }

    @Override
    public int compareTo(Hand other) {

        if (this == other) {
            return 0;
        }
        final se.cygni.texasholdem.game.Hand otherHand = other.hand;

        int result = hand.getPokerHand().getOrderValue() - otherHand.getPokerHand().getOrderValue();
        if (result == 0) {
            for (int i = 0; i < hand.getCards().size(); i++) {
                Card card = hand.getCards().get(i);
                Card otherCard = otherHand.getCards().get(i);
                result = card.getRank().getOrderValue() - otherCard.getRank().getOrderValue();
                if (result != 0) {
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Return how many of the pocket cards that are part of the {@link se.cygni.texasholdem.game.definitions.PokerHand}
     *
     * @param pocketCards pocket cards
     * @return number of pocket cards present in this hand
     */
    public int getNumberOfPocketCardsInPokerHand(final List<Card> pocketCards) {

        Set<Card> rankingSet = new HashSet<>(pocketCards);
        rankingSet.retainAll(hand.getCards().subList(0, hand.getPokerHand().getCardsRequired()));

        return rankingSet.size();
    }

    @Override
    public String toString() {
        return String.format("%s : %s", hand.getPokerHand(), collectionToCommaDelimitedString(hand.getCards()));
    }
}
