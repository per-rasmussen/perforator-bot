package se.cygni.texasholdem.player.postflop;

import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.player.stats.GameStatistics;

import java.util.List;

public interface PostFlopStrategy {
    Action getPostFlopAction(List<Action> possibleActions, HandRanking ranking, CurrentPlayState playState, GameStatistics gameStatistics);

    void incrementRaises();

    void resetRaises();
}
