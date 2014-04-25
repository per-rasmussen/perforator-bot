package se.cygni.texasholdem.player.postflop;

import se.cygni.texasholdem.game.Card;

import java.util.List;

public interface HandRankingService {

    int getCardsInRankingDeck();
    HandRanking getRanking(List<Card> pocketCards, List<Card> communityCards, int samples);

    void resetDeck();
}
