package se.cygni.texasholdem.player.preflop;

import se.cygni.texasholdem.game.Card;

import static java.util.EnumSet.*;
import static org.springframework.util.Assert.notNull;
import static se.cygni.texasholdem.game.definitions.Rank.*;
import static se.cygni.texasholdem.player.CardUtils.*;

public enum StartingHandType {

    ALWAYS_PLAY((c1, c2) ->
            isPairOf(c1, c2, range(SEVEN, ACE)) ||
            isCombinationOf(c1, c2, ACE, range(TEN, KING)) ||
            isCombinationOf(c1, c2, KING, range(JACK, QUEEN)) ||
            isSuitedCombinationOf(c1, c2, KING, of(TEN)) ||
            isSuitedCombinationOf(c1, c2, QUEEN, range(TEN, JACK)) ||
            isSuitedCombinationOf(c1, c2, JACK, range(NINE, TEN)) ||
            isSuitedCombinationOf(c1, c2, of(NINE, TEN))),

    MIDDLE_LATE_PLAY((c1, c2) ->
            isPairOf(c1, c2, of(FIVE, SIX)) ||
            isSuitedCombinationOf(c1, c2, ACE, range(SIX, NINE)) ||
            isSuitedCombinationOf(c1, c2, QUEEN, range(EIGHT, NINE)) ||
            isCombinationOf(c1, c2, QUEEN, range(TEN, JACK)) ||
            isSuitedCombinationOf(c1, c2, KING, of(NINE)) ||
            isSuitedCombinationOf(c1, c2, JACK, of(EIGHT)) ||
            isSuitedCombinationOf(c1, c2, NINE, of(EIGHT)) ||
            isSuitedCombinationOf(c1, c2, TEN, of(EIGHT)) ||
            isCombinationOf(c1, c2, KING, of(TEN)) ||
            isCombinationOf(c1, c2, JACK, of(TEN))),

    ONLY_LATE_PLAY((c1, c2) ->
            isPairOf(c1, c2, range(DEUCE, THREE)) ||
            isCombinationOf(c1, c2, ACE, range(SEVEN, NINE)) ||
            isSuitedCombinationOf(c1, c2, ACE, range(DEUCE, FIVE)) ||
            isSuitedCombinationOf(c1, c2, KING, range(DEUCE, EIGHT)) ||
            isCombinationOf(c1, c2, KING, of(NINE)) ||
            isCombinationOf(c1, c2, QUEEN, of(NINE)) ||
            isSuitedCombinationOf(c1, c2, JACK, of(SEVEN)) ||
            isCombinationOf(c1, c2, JACK, range(EIGHT, NINE)) ||
            isCombinationOf(c1, c2, TEN, range(EIGHT, NINE)) ||
            isCombinationOf(c1, c2, NINE, range(SEVEN, EIGHT)) ||
            isCombinationOf(c1, c2, EIGHT, of(SEVEN)) ||
            isSuitedCombinationOf(c1, c2, TEN, of(SEVEN)) ||
            isSuitedCombinationOf(c1, c2, NINE, range(SIX, SEVEN)) ||
            isSuitedCombinationOf(c1, c2, EIGHT, range(SIX, SEVEN)) ||
            isSuitedCombinationOf(c1, c2, SEVEN, range(FIVE, SIX)) ||
            isSuitedCombinationOf(c1, c2, SIX, of(FIVE)) ||
            isSuitedCombinationOf(c1, c2, FIVE, of(FOUR))),

    NO_PLAY(new PocketCardEvaluator() {
        @Override
        public boolean isInGroup(Card c1, Card c2) {
            // Check that combination is not in any other group
            for (StartingHandType g : complementOf(of(NO_PLAY))) {
                if (g.isInGroup(c1, c2)) {
                    return false;
                }
            }
            return true;
        }
    });


    private interface PocketCardEvaluator {
        boolean isInGroup(Card c1, Card c2);
    }

    private final PocketCardEvaluator evaluator;

    StartingHandType(final PocketCardEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    boolean isInGroup(Card c1, Card c2) {
        return evaluator.isInGroup(c1, c2);
    }

    public static StartingHandType getGroup(final Card c1, final Card c2) {
        notNull(c1, "'c1' cannot be null");
        notNull(c2, "'c2' cannot be null");

        for (StartingHandType type : allOf(StartingHandType.class)) {
            if (type.isInGroup(c1, c2)) {
                return type;
            }
        }

        throw new IllegalStateException("Could not get starting hand type of [" + c1 + ", " + c2 + "]");
    }

}
