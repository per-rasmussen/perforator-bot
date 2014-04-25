package se.cygni.texasholdem.player.preflop;

import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.game.Card;

import java.util.List;

public interface PreFlopStrategy {

    void putPocketCard(Card pocketCard);

    Action getPreFlopAction(List<Action> possibleActions);
}
