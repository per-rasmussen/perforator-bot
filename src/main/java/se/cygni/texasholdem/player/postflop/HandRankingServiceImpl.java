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
    private final Map<Integer, HandRanking> cachedRanking;

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
    public HandRanking getRanking(List<Card> pocketCards, List<Card> communityCards) {
        notNull(pocketCards, "'pocketCards' cannot be null");
        notNull(communityCards, "'communityCards' cannot be null");
        state(pocketCards.size() == 2, "should always contain two pocket cards");
        state(communityCards.size() > 2 && communityCards.size() < 6, "should always be between 2 and 5 cards");

        // Have we calculated a ranking?
        int numCards = pocketCards.size() + communityCards.size();
        if (cachedRanking.containsKey(numCards)) {
            return cachedRanking.get(numCards);
        }

        availableCards.removeAll(communityCards);
        availableCards.removeAll(pocketCards);

        // create a bunch of permutations of pocket cards from the available ones.
        Card[] available = availableCards.toArray(new Card[0]);

        List<Hand> hands = new LinkedList<>();
        // Brute force of all other permutations of pocket cards.
        for (int c1Idx = 0; c1Idx < available.length; c1Idx++) {
            Card card1 = available[c1Idx];
            for (int c2Idx = c1Idx + 1; c2Idx < available.length; c2Idx++) {
                Card card2 = available[c2Idx];
                hands.add(new Hand(new PokerHandUtils(communityCards, asList(card1, card2)).getBestHand()));
            }
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
