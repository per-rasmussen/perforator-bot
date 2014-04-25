package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Rank;

import java.util.EnumSet;

import static java.util.Arrays.asList;

public class CardUtils {

    public static boolean isCombinationOf(Card c1, Card c2, EnumSet<Rank> setOf) {
        return setOf.containsAll(asList(c1.getRank(), c2.getRank()));
    }

    public static boolean isSuitedCombinationOf(Card c1, Card c2, EnumSet<Rank> setOf) {
        return c1.getSuit() == c2.getSuit() && isCombinationOf(c1, c2, setOf);
    }

    public static boolean isCombinationOf(Card c1, Card c2, Rank rank, EnumSet<Rank> setOf) {
        return (c1.getRank() == rank && setOf.contains(c2.getRank())) ||
                (c2.getRank() == rank && setOf.contains(c1.getRank()));
    }

    public static boolean isSuitedCombinationOf(Card c1, Card c2, Rank rank, EnumSet<Rank> setOf) {
        return c1.getSuit() == c2.getSuit() && isCombinationOf(c1, c2, rank, setOf);
    }

    public static boolean isPairOf(Card c1, Card c2, EnumSet<Rank> pairEnumSet) {
        return c1.getRank() == c2.getRank() && pairEnumSet.containsAll(asList(c1.getRank(), c2.getRank()));
    }

}
