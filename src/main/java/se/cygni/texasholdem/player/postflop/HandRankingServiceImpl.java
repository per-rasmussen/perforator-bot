package se.cygni.texasholdem.player.postflop;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.Deck;
import se.cygni.texasholdem.player.utils.PokerHandUtils;

import java.util.*;

import static java.util.Arrays.asList;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

public class HandRankingServiceImpl implements HandRankingService {

    private Set<Card> availableCards;
    private final Random random = new Random(System.currentTimeMillis());
    private Map<Integer, HandRanking> cachedRanking;

    public HandRankingServiceImpl() {
        this.availableCards = getShuffledDeckOfCards();
        this.cachedRanking = new HashMap<>();
    }

    private Set<Card> getShuffledDeckOfCards() {
        Set<Card> newDeck = new HashSet<>();
        Deck deck = Deck.getShuffledDeck();
        while (deck.getCardsLeft() > 0) {
            newDeck.add(deck.getNextCard());
        }
        return newDeck;
    }

    public void resetDeck() {
        availableCards = getShuffledDeckOfCards();
        cachedRanking.clear();
    }


    @Override
    public int getCardsInRankingDeck() {
        return availableCards.size();
    }

    @Override
    public HandRanking getRanking(List<Card> pocketCards, List<Card> communityCards, int samples) {
        notNull(pocketCards, "'pocketCards' cannot be null");
        notNull(communityCards, "'communityCards' cannot be null");
        state(pocketCards.size() == 2, "should always contain two hole cards");
        state(communityCards.size() > 2 && communityCards.size() < 6, "should always be between 2 and 5 cards");

        // Have we calculated a ranking
        int numCards = pocketCards.size() + communityCards.size();
        if (cachedRanking.containsKey(numCards)) {
            cachedRanking.get(numCards);
        }

        availableCards.removeAll(communityCards);
        availableCards.removeAll(pocketCards);

        // create a bunch of permutations of pocket cards from the available ones.
        Card[] available = availableCards.toArray(new Card[availableCards.size()]);
        int size = availableCards.size() - 1;
        List<Hand> hands = new LinkedList<>();
        for (int i = 0; i < samples; i++) {
            List<Card> holeCardPerm = asList(
                    available[random.nextInt(size)],
                    available[random.nextInt(size)]);
            hands.add(new Hand(new PokerHandUtils(communityCards, holeCardPerm).getBestHand()));
        }

        Hand myHand = new Hand(new PokerHandUtils(communityCards, pocketCards).getBestHand());
        hands.add(myHand);

        Collections.sort(hands);
        int i = hands.indexOf(myHand);

        int ranking = ((int) ((double) i / hands.size() * 100));

        HandRanking handRanking = new HandRanking(myHand, ranking, myHand.getNumberOfPocketCardsInPokerHand(pocketCards));
        cachedRanking.put(numCards, handRanking);

        return handRanking;
    }
}
