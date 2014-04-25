package se.cygni.texasholdem.player;

import org.junit.Before;
import org.junit.Test;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.player.postflop.HandRanking;
import se.cygni.texasholdem.player.postflop.HandRankingService;
import se.cygni.texasholdem.player.postflop.HandRankingServiceImpl;

import java.util.List;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static se.cygni.texasholdem.game.definitions.Rank.*;
import static se.cygni.texasholdem.game.definitions.Suit.*;

public class HandRankingServiceTest {

    private static final Card DEUCE_OF_CLUBS = new Card(DEUCE, CLUBS);
    private static final Card ACE_OF_DIAMONDS = new Card(ACE, DIAMONDS);
    private static final Card ACE_OF_HEARTS = new Card(ACE, HEARTS);
    private static final Card ACE_OF_CLUBS = new Card(ACE, CLUBS);
    private static final Card SEVEN_OF_DIAMONDS = new Card(SEVEN, DIAMONDS);
    private static final Card SEVEN_OF_CLUBS = new Card(SEVEN, CLUBS);
    private static final Card EIGHT_OF_CLUBS = new Card(EIGHT, CLUBS);
    private static final Card SIX_OF_CLUBS = new Card(SIX, CLUBS);
    private static final Card SIX_OF_SPADES = new Card(SIX, SPADES);


    private static final Card DEUCE_OF_HEARTS = new Card(DEUCE, HEARTS);

    private static final Card TEN_OF_HEARTS = new Card(TEN, DIAMONDS);
    private HandRankingService target;

    @Before
    public void setUp() {
        target = new HandRankingServiceImpl();
    }


    @Test
    public void shouldGetCardsInDeck() {

        // fixtures
        int cardsInDeck = 52;

        // test

        // verify
        assertEquals(cardsInDeck, target.getCardsInRankingDeck());

    }

    @Test
    public void shouldTestCardsInAvailableDeckOnInit() {

        // fixtures
        int cardsInDeck = 52;

        // test

        // verify
        assertEquals(cardsInDeck, target.getCardsInRankingDeck());

    }

    @Test
    public void shouldTestCardsInAvailableDeckOnFlop() {

        // fixtures
        int cardsInDeck = 52 - 2 - 3;
        List<Card> communityCards = asList(DEUCE_OF_CLUBS, ACE_OF_DIAMONDS, SEVEN_OF_DIAMONDS);
        List<Card> holeCards = asList(SEVEN_OF_CLUBS, ACE_OF_HEARTS);

        // test
        HandRanking ranking = target.getRanking(holeCards, communityCards, 1000);

        // verify
        assertEquals(cardsInDeck, target.getCardsInRankingDeck());
        assertTrue(ranking.getRankingValue() > 97);

    }

    @Test
    public void shouldTestRankingWhenOnePocketAndFullHouseOnTurn() {

        // fixtures
        int cardsInDeck = 52 - 2 - 4;
        List<Card> communityCards = asList(ACE_OF_CLUBS, ACE_OF_HEARTS, SIX_OF_SPADES, SIX_OF_CLUBS);
        List<Card> holeCards = asList(TEN_OF_HEARTS, ACE_OF_DIAMONDS);

        // test
        HandRanking ranking = target.getRanking(holeCards, communityCards, 10000);

        // verify
        assertEquals(cardsInDeck, target.getCardsInRankingDeck());
        assertTrue(ranking.getRankingValue() > 97);

    }

    @Test
    public void shouldTestCardsInAvailableDeckOnTurn() {

        // fixtures
        int cardsInDeck = 52 - 2 - 4;
        List<Card> communityCards = asList(DEUCE_OF_CLUBS, ACE_OF_DIAMONDS, SEVEN_OF_DIAMONDS, ACE_OF_HEARTS);
        List<Card> holeCards = asList(SEVEN_OF_CLUBS, EIGHT_OF_CLUBS);

        // test
        HandRanking ranking = target.getRanking(holeCards, communityCards, 1000);

        // verify
        assertEquals(cardsInDeck, target.getCardsInRankingDeck());
        assertTrue(ranking.getRankingValue() > 79);

    }

    @Test
    public void shouldTestCardsInAvailableDeckOnRiver() {

        // fixtures
        int cardsInDeck = 52 - 2 - 5;
        List<Card> communityCards = asList(DEUCE_OF_CLUBS, ACE_OF_DIAMONDS, SEVEN_OF_DIAMONDS, ACE_OF_HEARTS, DEUCE_OF_HEARTS);
        List<Card> holeCards = asList(SEVEN_OF_CLUBS, EIGHT_OF_CLUBS);

        // test
        HandRanking ranking = target.getRanking(holeCards, communityCards, 1000);

        // verify
        assertEquals(cardsInDeck, target.getCardsInRankingDeck());
        assertTrue(ranking.getRankingValue() > 72);

    }
}
