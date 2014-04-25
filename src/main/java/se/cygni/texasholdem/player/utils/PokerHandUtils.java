package se.cygni.texasholdem.player.utils;

import org.apache.commons.collections.CollectionUtils;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.Hand;
import se.cygni.texasholdem.game.definitions.CardSortBy;
import se.cygni.texasholdem.game.definitions.PokerHand;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.*;
import java.util.Map.Entry;

import static org.springframework.util.Assert.state;
import static se.cygni.texasholdem.player.utils.CardUtils.*;

public class PokerHandUtils {

    private final Map<Rank, List<Card>> rankDistribution;
    private final Map<Suit, List<Card>> suitDistribution;
    private final List<Card> cards;

    public PokerHandUtils(final List<Card> communityCards,
                          final List<Card> playerCards) {

        cards = merge(communityCards, playerCards);
        rankDistribution = getRankDistribution(cards);
        suitDistribution = getSuitDistribution(cards);
    }

    /*public PokerHandUtils(final List<Card> cards) {

        this.cards = cards;
        rankDistribution = getRankDistribution(cards);
        suitDistribution = getSuitDistribution(cards);
    } */

    /**
     * Extracts the best possible hand and returns the type of PokerHand and the
     * List of cards that make that hand.
     *
     * @return The Best Hand given the current list of cards
     */
    public Hand getBestHand() {

        // For royal flush, straight flush and flush find suit with at least 5 cards.
        List<Card> suitOfAtLeastFiveCards = getPossibleSuitOfFiveCards();
        Hand hand;

        if (suitOfAtLeastFiveCards != null) {

            hand = isRoyalFlush(suitOfAtLeastFiveCards);
            if (hand != null) {
                return hand;
            }

            hand = isStraightFlush(suitOfAtLeastFiveCards);
            if (hand != null) {
                return hand;
            }

            hand = isFlush(suitOfAtLeastFiveCards);
            if (hand != null) {
                return hand;
            }
        }

        hand = isFourOfAKind();
        if (hand != null) {
            return hand;
        }

        hand = isFullHouse();
        if (hand != null) {
            return hand;
        }


        hand = isStraight();
        if (hand != null) {
            return hand;
        }

        hand = isThreeOfAKind();
        if (hand != null) {
            return hand;
        }

        hand = isTwoPairs();
        if (hand != null) {
            return hand;
        }

        hand = isOnePair();
        if (hand != null) {
            return hand;
        }

        return isHighHand();
    }

    public List<Card> getPossibleSuitOfFiveCards() {

        for (final Entry<Suit, List<Card>> entry : suitDistribution.entrySet()) {
            if (entry.getValue().size() >= 5) {
                return entry.getValue();
            }
        }
        return null;
    }


    protected Hand isRoyalFlush(List<Card> potentialRoyalFlush) {
        state(potentialRoyalFlush != null && potentialRoyalFlush.size() > 4, "suit too small");

        potentialRoyalFlush = getHighestSortedAndExclude(5, potentialRoyalFlush, null);

        int counter = PokerHand.ROYAL_FLUSH.getCardsRequired() - 1;
        for (final Rank r : EnumSet.range(Rank.TEN, Rank.ACE)) {
            final Card c = potentialRoyalFlush.get(counter--);
            if (r != c.getRank()) {
                return null;
            }
        }

        return new Hand(potentialRoyalFlush, PokerHand.ROYAL_FLUSH);
    }

    protected Hand isStraightFlush(List<Card> potentialStraightFlush) {
        state(potentialStraightFlush != null && potentialStraightFlush.size() > 4, "suit too small");

        potentialStraightFlush = getLongestConsecutiveSubset(potentialStraightFlush);
        final int size = potentialStraightFlush.size();
        if (size < PokerHand.STRAIGHT_FLUSH.getCardsRequired()) {
            return null;
        }

        if (size > PokerHand.STRAIGHT_FLUSH.getCardsRequired()) {
            potentialStraightFlush = potentialStraightFlush.subList(size - PokerHand.STRAIGHT_FLUSH.getCardsRequired(),
                    size);
        }

        Collections.reverse(potentialStraightFlush);

        return new Hand(potentialStraightFlush, PokerHand.STRAIGHT_FLUSH);
    }

    protected Hand isFourOfAKind() {

        // Is there a rank that contains 4 cards?
        List<Card> potentialFourOfAKind = null;
        for (final Entry<Rank, List<Card>> entry : rankDistribution.entrySet()) {
            if (entry.getValue().size() == PokerHand.FOUR_OF_A_KIND.getCardsRequired()) {
                potentialFourOfAKind = entry.getValue();
                break;
            }
        }

        if (potentialFourOfAKind == null) {
            return null;
        }

        // Sort by suit
        Collections.sort(potentialFourOfAKind, CardSortBy.SUIT.getComparator());
        potentialFourOfAKind.addAll(getHighestSortedAndExclude(
                5 - PokerHand.FOUR_OF_A_KIND.getCardsRequired(), cards,
                potentialFourOfAKind));

        return new Hand(potentialFourOfAKind, PokerHand.FOUR_OF_A_KIND);
    }

    protected Hand isFullHouse() {

        final List<Card> highestThreeOfAKind = getHighestOfSameRank(PokerHand.THREE_OF_A_KIND.getCardsRequired(), cards);
        if (CollectionUtils.isEmpty(highestThreeOfAKind)) {
            return null;
        }

        final List<Card> highestTwoOfAKind = getHighestOfSameRankExcluding(PokerHand.ONE_PAIR.getCardsRequired(),
                cards, highestThreeOfAKind.get(0).getRank());

        if (CollectionUtils.isEmpty(highestTwoOfAKind)) {
            return null;
        }

        final List<Card> fullHouse = new ArrayList<Card>();
        fullHouse.addAll(highestThreeOfAKind);
        fullHouse.addAll(highestTwoOfAKind);

        return new Hand(fullHouse, PokerHand.FULL_HOUSE);
    }

    protected Hand isFlush(List<Card> potentialFlush) {

        potentialFlush = getHighestSortedAndExclude(
                PokerHand.FLUSH.getCardsRequired(), potentialFlush, null);

        return new Hand(potentialFlush, PokerHand.FLUSH);

    }

    protected Hand isStraight() {

        List<Card> potentialStraight = getLongestConsecutiveSubset(cards);

        if (potentialStraight.size() < PokerHand.STRAIGHT.getCardsRequired()) {
            return null;
        }

        final int size = potentialStraight.size();
        if (size > PokerHand.STRAIGHT.getCardsRequired()) {
            potentialStraight = potentialStraight.subList(
                    size - PokerHand.STRAIGHT.getCardsRequired(), size);
        }

        Collections.reverse(potentialStraight);

        return new Hand(potentialStraight, PokerHand.STRAIGHT);
    }

    protected Hand isThreeOfAKind() {

        // Is there a rank that contains 3 cards?
        List<Card> potentialThreeOfAKind = null;
        for (final Entry<Rank, List<Card>> entry : rankDistribution.entrySet()) {
            if (entry.getValue().size() == PokerHand.THREE_OF_A_KIND.getCardsRequired()) {

                final List<Card> threeOfAKind = entry.getValue();

                // There might be more than one set of three-of-a-kind, choose
                // the highest ranking one.
                if (potentialThreeOfAKind != null) {
                    if (potentialThreeOfAKind.get(0).getRank().getOrderValue() < threeOfAKind
                            .get(0).getRank().getOrderValue()) {
                        potentialThreeOfAKind = threeOfAKind;
                    }
                } else {
                    potentialThreeOfAKind = entry.getValue();
                }
            }
        }

        if (CollectionUtils.isEmpty(potentialThreeOfAKind)) {
            return null;
        }

        Collections.sort(potentialThreeOfAKind, CardSortBy.SUIT.getComparator());
        potentialThreeOfAKind.addAll(getHighestSortedAndExclude(
                5 - PokerHand.THREE_OF_A_KIND.getCardsRequired(), cards,
                potentialThreeOfAKind));

        return new Hand(potentialThreeOfAKind, PokerHand.THREE_OF_A_KIND);
    }

    protected Hand isTwoPairs() {

        final List<Card> highestTwoOfAKind = getHighestOfSameRank(
                PokerHand.ONE_PAIR.getCardsRequired(), cards);

        if (CollectionUtils.isEmpty(highestTwoOfAKind)) {
            return null;
        }

        final List<Card> nextHighestTwoOfAKind = getHighestOfSameRankExcluding(
                PokerHand.ONE_PAIR.getCardsRequired(), cards, highestTwoOfAKind.get(0).getRank());

        if (CollectionUtils.isEmpty(nextHighestTwoOfAKind)) {
            return null;
        }

        final List<Card> twoPairs = new ArrayList<Card>();
        twoPairs.addAll(highestTwoOfAKind);
        twoPairs.addAll(nextHighestTwoOfAKind);
        twoPairs.addAll(getHighestSortedAndExclude(
                5 - PokerHand.TWO_PAIRS.getCardsRequired(), cards, twoPairs));

        return new Hand(twoPairs, PokerHand.TWO_PAIRS);
    }

    protected Hand isOnePair() {

        // Is there a rank that contains 2 cards and not 3 or 4?
        List<Card> potentialOnePair = null;
        for (final Entry<Rank, List<Card>> entry : rankDistribution.entrySet()) {
            if (entry.getValue().size() == 2) {

                if (potentialOnePair != null) {
                    return null;
                }

                potentialOnePair = entry.getValue();
            } else if (entry.getValue().size() > 2) {
                return null;
            }
        }

        if (potentialOnePair == null) {
            return null;
        }

        // The best hand is the one pair plus the highest other cards.
        final List<Card> restOfCards = getHighestSortedAndExclude(
                5 - PokerHand.ONE_PAIR.getCardsRequired(), cards,
                potentialOnePair);

        // Sort by suit
        Collections.sort(potentialOnePair, CardSortBy.SUIT.getComparator());
        potentialOnePair.addAll(restOfCards);

        return new Hand(potentialOnePair, PokerHand.ONE_PAIR);
    }

    protected Hand isHighHand() {

        return new Hand(getHighestSortedAndExclude(5, cards, null), PokerHand.HIGH_HAND);
    }

}
