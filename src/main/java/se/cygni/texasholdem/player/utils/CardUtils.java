package se.cygni.texasholdem.player.utils;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.CardSortBy;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.*;

/**
 * A set of utilities for searching and manipulating a List of Cards.
 *
 * @author emil
 */
public final class CardUtils {

    private CardUtils() {
    }

    /**
     * Creates a map where cards are organized according to their Rank.
     *
     * @param cards list of cards
     * @return a map where cards are organized according to their Rank
     */
    protected static Map<Rank, List<Card>> getRankDistribution(final List<Card> cards) {

        final Map<Rank, List<Card>> distribution = new TreeMap<>();
        for (final Card c : cards) {
            if (distribution.containsKey(c.getRank())) {
                distribution.get(c.getRank()).add(c);
            } else {
                final ArrayList<Card> l = new ArrayList<>(4);
                l.add(c);
                distribution.put(c.getRank(), l);
            }
        }
        return distribution;
    }

    /**
     * Creates a map where cards are organized according to their Suit.
     *
     * @param cards list of cards
     * @return a map where cards are organized according to their Suit
     */
    protected static Map<Suit, List<Card>> getSuitDistribution(final List<Card> cards) {

        final Map<Suit, List<Card>> distribution = new TreeMap<>();
        for (final Card c : cards) {
            if (distribution.containsKey(c.getSuit())) {
                distribution.get(c.getSuit()).add(c);
            } else {
                final ArrayList<Card> l = new ArrayList<>(7);
                l.add(c);
                distribution.put(c.getSuit(), l);
            }
        }
        return distribution;
    }

    /**
     * Null safe merge of two lists. If both lists are empty or null an empty
     * list is returned.
     *
     * @param first the first list
     * @param second the second list
     * @return The merge of the lists first and second
     */
    protected static List<Card> merge(final List<Card> first, final List<Card> second) {

        final List<Card> result = new ArrayList<>(7);

        if (first != null) {
            result.addAll(first);
        }

        if (second != null) {
            result.addAll(second);
        }

        return result;
    }

    /**
     * Creates a new list containing only the cards in target that don't exist
     * in remove. Null safe. If target is empty or null an empty list will be
     * returned.
     *
     * @param target target list
     * @param remove cards to remove from target
     * @return A copy of the target list with all elements from remove removed.
     */
    protected static List<Card> remove(final List<Card> target, final List<Card> remove) {

        final List<Card> result = new ArrayList<>(7);

        if (remove == null || remove.isEmpty()) {
            result.addAll(target);
            return result;
        }

        for (final Card c : target) {
            if (!remove.contains(c)) {
                result.add(c);
            }
        }

        return result;
    }

    /**
     * Sorts the list of cards by Rank in descending order and removes any
     * occurrences of cards in the list of excluded cards.
     * <p/>
     * Example: getHighestSortedAndExclude(2, {5d, 9c, Jd, 2c, Qs}, {Qs, 9c})
     * Result: {Jd, 5d}
     *
     * @param noof    The max length of the resulting list of cards
     * @param cards
     * @param exclude A list of cards to exclude from the result
     * @return A descending (by Rank) list of cards
     */
    protected static List<Card> getHighestSortedAndExclude(
            final int noof,
            final List<Card> cards,
            final List<Card> exclude) {

        final List<Card> removed = remove(cards, exclude);
        removed.sort(CardSortBy.RANK.getComparator());
        Collections.reverse(removed);

        if (noof > removed.size()) {
            return removed;
        }

        return removed.subList(0, noof);
    }

    /**
     * Sorts the list of cards in a new List.
     *
     * @param sort order to sort
     * @param cards cards to sort
     * @return A new List sorted by Rank och Suit.
     */
    protected static List<Card> sortBy(
            final CardSortBy sort,
            final List<Card> cards) {

        final List<Card> result = new ArrayList<>(cards);
        result.sort(sort.getComparator());
        return result;
    }

    /**
     * Returns the longest possible list of cards in Rank order. A card ranked
     * ACE can be both at the beginning of a list or at the end. If no
     * consecutive list can be found in the list of cards an empty list is
     * returned.
     * <p/>
     * Example: getLongestConsecutiveSubset({5h, 3s, Ad, As, 2c, Kh, 4c})
     * Returns: {Ad, 2c, 3s, 4c, 5h}
     *
     * @param cards cards
     * @return The longest consecutive sublist of cards found or the empty list
     *         if none found.
     */
    protected static List<Card> getLongestConsecutiveSubset(final List<Card> cards) {

        final List<Card> sortedCards = removeDuplicatesByRankAndSortByRank(cards);

        // If an ACE exists in cards place it at beginning as well since
        // in a straight it may act as numeric value 14 or 1.
        final Card lastCard = sortedCards.get(sortedCards.size() - 1);
        if (lastCard.getRank() == Rank.ACE) {
            sortedCards.add(0, lastCard);
        }

        List<Card> largestConsecutive = new ArrayList<>();

        int currStartPos = 0;
        Card previousCard = null;
        for (int i = 0; i < sortedCards.size(); i++) {
            final Card currentCard = sortedCards.get(i);

            if (previousCard == null ||
                    // If the previous card value + 1 is not equal to the
                    // current card's value the order is broken. Unless...
                    ((previousCard.getRank().getOrderValue() + 1 != currentCard
                            .getRank().getOrderValue()) &&
                            // ... the previous card was an ACE and the current
                            // card is valued 2.
                            !(previousCard.getRank() == Rank.ACE && currentCard.getRank()
                                    .getOrderValue() == 2))) {

                // Found consecutive subset larger than one,
                // check if it is longer than the previously
                // known one.
                if (currStartPos < i - 1
                        && i - currStartPos > largestConsecutive.size() - 1) {
                    largestConsecutive = sortedCards.subList(currStartPos, i);
                }

                currStartPos = i;
            }

            previousCard = currentCard;
        }

        // In case the end is part of the largest consecutive sublist
        if (currStartPos < sortedCards.size() - 1
                && sortedCards.size() - 1 - currStartPos > largestConsecutive
                .size() - 1) {
            largestConsecutive = sortedCards.subList(currStartPos,
                    sortedCards.size());
        }

        return largestConsecutive;
    }

    /**
     * Sorts the list of cards by Rank and then removes any cards of the same
     * Rank.
     * <p/>
     * Example: removeDuplicatesByRankAndSortByRank({5h, Jd, 3s, 5d, Js}
     * Returns: {3s, 5d, Jd}
     *
     * @param cards cards
     * @return A sorted list of Cards with no duplicate Ranks.
     */
    protected static List<Card> removeDuplicatesByRankAndSortByRank(final List<Card> cards) {

        final List<Card> sortedCards = sortBy(CardSortBy.RANK, cards);
        final List<Card> result = new ArrayList<>(7);
        Card previousCard = null;

        for (final Card c : sortedCards) {
            if (previousCard == null || previousCard.getRank() != c.getRank()) {
                result.add(c);
            }

            previousCard = c;
        }
        return result;
    }

    /**
     * Searches a List of Cards for cards of the same rank that exist in least
     * noof times and returns the highest ranking list, excluded any Card of the
     * excluded Rank.
     *
     * @param noof         The minimum number of cards of same rank
     * @param cards        The list of cards available
     * @param excludedRank A rank to exclude from the result
     * @return A list of cards of noof length of the highest rank.
     * @see #getHighestOfSameRank(int, List)
     */
    protected static List<Card> getHighestOfSameRankExcluding(
            final int noof,
            final List<Card> cards,
            final Rank excludedRank) {

        final List<Card> activeCardList = new ArrayList<>(7);
        for (final Card c : cards) {
            if (c.getRank() != excludedRank) {
                activeCardList.add(c);
            }
        }

        return getHighestOfSameRank(noof, activeCardList);
    }

    /**
     * Searches a List of Cards for cards of the same rank that exist in least
     * noof times and returns the highest ranking list.
     * <p/>
     * Example: getHighestOfSameRank(2, {5h, Jd, 3s, 5d, Js} Returns: {Jd, Js}
     *
     * @param noof  The minimum number of cards of same rank
     * @param cards The list of cards available
     * @return A list of cards of noof length of the highest rank.
     */
    protected static List<Card> getHighestOfSameRank(
            final int noof,
            final List<Card> cards) {

        List<Card> result = new ArrayList<>(7);

        Rank currBestRank = null;
        final Map<Rank, List<Card>> distribution = getRankDistribution(cards);
        for (final Map.Entry<Rank, List<Card>> entry : distribution.entrySet()) {
            if (entry.getValue().size() >= noof) {
                if (currBestRank == null
                        || currBestRank.compareTo(entry.getKey()) < 0) {
                    currBestRank = entry.getKey();
                    result = entry.getValue();
                }
            }
        }
        result.sort(CardSortBy.SUIT.getComparator());
        if (result.size() > noof) {
            result = result.subList(0, noof);
        }

        return result;
    }
}

