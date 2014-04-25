package se.cygni.texasholdem.player;

import org.junit.Test;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Suit;
import se.cygni.texasholdem.player.preflop.StartingHandType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static se.cygni.texasholdem.game.definitions.Rank.*;

public class StartingHandGroupTest {

    private static final Card ACE_OF_CLUBS = new Card(ACE, Suit.CLUBS);
    private static final Card ACE_OF_HEARTS = new Card(ACE, Suit.HEARTS);
    private static final Card KING_OF_CLUBS = new Card(KING, Suit.CLUBS);
    private static final Card KING_OF_HEARTS = new Card(KING, Suit.HEARTS);
    private static final Card QUEEN_OF_HEARTS = new Card(QUEEN, Suit.HEARTS);
    private static final Card QUEEN_OF_CLUBS = new Card(QUEEN, Suit.CLUBS);
    private static final Card JACK_OF_CLUBS = new Card(JACK, Suit.CLUBS);
    private static final Card JACK_OF_SPADES = new Card(JACK, Suit.SPADES);
    private static final Card TEN_OF_DIAMONDS = new Card(TEN, Suit.DIAMONDS);
    private static final Card TEN_OF_HEARTS = new Card(TEN, Suit.HEARTS);
    private static final Card NINE_OF_DIAMONDS = new Card(NINE, Suit.DIAMONDS);
    private static final Card NINE_OF_HEARTS = new Card(NINE, Suit.HEARTS);
    private static final Card EIGHT_OF_DIAMONDS = new Card(EIGHT, Suit.DIAMONDS);
    private static final Card EIGHT_OF_HEARTS = new Card(EIGHT, Suit.HEARTS);
    private static final Card DEUCE_OF_HEARTS = new Card(DEUCE, Suit.HEARTS);
    private static final Card THREE_OF_HEARTS = new Card(THREE, Suit.HEARTS);
    private static final Card FIVE_OF_HEARTS = new Card(FIVE, Suit.HEARTS);

    @Test
    public void shouldGetAlwaysGroup() {

        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(ACE_OF_CLUBS, ACE_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(KING_OF_CLUBS, KING_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(ACE_OF_CLUBS, KING_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(QUEEN_OF_HEARTS, QUEEN_OF_CLUBS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(JACK_OF_CLUBS, JACK_OF_SPADES));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(TEN_OF_DIAMONDS, TEN_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(NINE_OF_DIAMONDS, NINE_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(EIGHT_OF_DIAMONDS, EIGHT_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(ACE_OF_CLUBS, KING_OF_HEARTS));
        assertEquals(StartingHandType.ALWAYS_PLAY, StartingHandType.getGroup(ACE_OF_CLUBS, QUEEN_OF_HEARTS));
    }

    @Test
    public void shouldGetMiddleGroup() {

        assertEquals(StartingHandType.MIDDLE_LATE_PLAY, StartingHandType.getGroup(NINE_OF_HEARTS, ACE_OF_HEARTS));
        assertNotEquals(StartingHandType.MIDDLE_LATE_PLAY, StartingHandType.getGroup(JACK_OF_CLUBS, QUEEN_OF_CLUBS));
    }

    @Test
    public void shouldGetLateGroup() {

        assertEquals(StartingHandType.ONLY_LATE_PLAY, StartingHandType.getGroup(ACE_OF_CLUBS, EIGHT_OF_HEARTS));
        assertEquals(StartingHandType.ONLY_LATE_PLAY, StartingHandType.getGroup(NINE_OF_HEARTS, EIGHT_OF_DIAMONDS));
        assertEquals(StartingHandType.ONLY_LATE_PLAY, StartingHandType.getGroup(QUEEN_OF_CLUBS, NINE_OF_DIAMONDS));
    }

    @Test
    public void shouldGetNoPlayGroup() {

        assertEquals(StartingHandType.NO_PLAY, StartingHandType.getGroup(DEUCE_OF_HEARTS, TEN_OF_HEARTS));
        assertEquals(StartingHandType.NO_PLAY, StartingHandType.getGroup(DEUCE_OF_HEARTS, THREE_OF_HEARTS));
        assertEquals(StartingHandType.NO_PLAY, StartingHandType.getGroup(FIVE_OF_HEARTS, THREE_OF_HEARTS));
        assertEquals(StartingHandType.NO_PLAY, StartingHandType.getGroup(EIGHT_OF_DIAMONDS, THREE_OF_HEARTS));
    }


}
