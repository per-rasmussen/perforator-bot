package se.cygni.texasholdem.player.preflop;

import org.junit.Test;
import se.cygni.texasholdem.game.Card;

import static org.junit.Assert.assertEquals;
import static se.cygni.texasholdem.game.Card.valueOf;
import static se.cygni.texasholdem.game.definitions.Rank.*;
import static se.cygni.texasholdem.game.definitions.Suit.*;
import static se.cygni.texasholdem.player.preflop.StealType.*;

public class StealTypeTest {

    @Test
    public void shouldTestNoSteal() {

        Card sixOfHearts = valueOf(SIX, HEARTS);
        Card queenOfDiamonds = valueOf(QUEEN, DIAMONDS);
        Card deuceOfSpades = valueOf(DEUCE, SPADES);

        assertEquals(DONT_STEAL, getType(sixOfHearts, queenOfDiamonds));
        assertEquals(DONT_STEAL, getType(deuceOfSpades, queenOfDiamonds));

    }

    @Test
    public void shouldTestSteal() {

        Card aceOfHearts = valueOf(ACE, HEARTS);
        Card queenOfDiamonds = valueOf(QUEEN, DIAMONDS);
        Card queenOfHearts = valueOf(QUEEN, HEARTS);
        Card deuceOfSpades = valueOf(DEUCE, SPADES);

        assertEquals(STEAL, getType(queenOfHearts, queenOfDiamonds));
        assertEquals(STEAL, getType(deuceOfSpades, aceOfHearts));

    }
}