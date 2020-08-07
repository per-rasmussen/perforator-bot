package se.cygni.texasholdem.player.preflop;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.player.utils.PlayState;

import java.util.List;
import java.util.Random;

import static java.lang.System.currentTimeMillis;
import static se.cygni.texasholdem.player.preflop.StealType.STEAL;
import static se.cygni.texasholdem.player.preflop.StealType.getType;

public class StealBlindsEvaluator {

    final private Random randomizer = new Random(currentTimeMillis());

    public boolean shouldSteal(PlayState ps) {
        List<Card> myCards = ps.getMyCards();
        int threshold = randomizer.nextInt(100);

        return ps.amIDealerPlayer() &&
                ps.getNumberOfPlayers() > 3 &&
                ps.getNumberOfPlayers() - ps.getNumberOfFoldedPlayers() == 3 &&
                getType(myCards.get(0), myCards.get(1)) == STEAL &&
                threshold > 50;
    }
}
