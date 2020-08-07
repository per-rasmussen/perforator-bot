package se.cygni.texasholdem.player.preflop;

import se.cygni.texasholdem.game.Card;

import static java.util.EnumSet.*;
import static se.cygni.texasholdem.game.definitions.Rank.*;
import static se.cygni.texasholdem.player.CardUtils.*;

public enum StealType {

    STEAL((c1, c2) -> isPairOf(c1, c2, range(DEUCE, ACE)) ||
            isCombinationOf(c1, c2, ACE, range(DEUCE, KING)) ||
            isSuitedCombinationOf(c1, c2, KING, range(SEVEN, QUEEN)) ||
            isSuitedCombinationOf(c1, c2, QUEEN, range(EIGHT, JACK)) ||
            isSuitedCombinationOf(c1, c2, JACK, range(SEVEN, TEN)) ||
            isSuitedCombinationOf(c1, c2, TEN, range(SEVEN, NINE)) ||
            isSuitedCombinationOf(c1, c2, NINE, range(SIX, EIGHT)) ||
            isSuitedCombinationOf(c1, c2, EIGHT, range(FIVE, SEVEN)) ||
            isSuitedCombinationOf(c1, c2, SEVEN, range(FIVE, SIX)) ||
            isSuitedCombinationOf(c1, c2, SIX, range(FOUR, FIVE)) ||
            isSuitedCombinationOf(c1, c2, FIVE, of(FOUR)) ||
            isCombinationOf(c1, c2, KING, range(NINE, QUEEN)) ||
            isCombinationOf(c1, c2, QUEEN, range(NINE, JACK)) ||
            isCombinationOf(c1, c2, JACK, range(EIGHT, TEN)) ||
            isCombinationOf(c1, c2, TEN, range(EIGHT, NINE)) ||
            isCombinationOf(c1, c2, NINE, of(EIGHT))),

    DONT_STEAL(new StealEvaluator() {
        @Override
        public boolean isInStealType(Card c1, Card c2) {
            // Check that combination is not in any other group
            for (StealType st : complementOf(of(DONT_STEAL))) {
                if (st.isInStealType(c1, c2)) {
                    return false;
                }
            }
            return true;
        }
    });

    private interface StealEvaluator {
        boolean isInStealType(Card c1, Card c2);
    }

    private final StealEvaluator evaluator;

    StealType(final StealEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    private boolean isInStealType(Card c1, Card c2) {
        return evaluator.isInStealType(c1, c2);
    }

    public static StealType getType(final Card c1, final Card c2) {

        for (StealType type : allOf(StealType.class)) {
            if (type.isInStealType(c1, c2)) {
                return type;
            }
        }

        throw new IllegalStateException("Could not get steal type");
    }


}
