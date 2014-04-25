package se.cygni.texasholdem.player.postflop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.game.ActionType;
import se.cygni.texasholdem.player.stats.GameStatistics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.EnumSet.of;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;
import static se.cygni.texasholdem.game.ActionType.*;
import static se.cygni.texasholdem.game.definitions.PlayState.FLOP;
import static se.cygni.texasholdem.game.definitions.PlayState.TURN;
import static se.cygni.texasholdem.player.ActionUtils.chooseInOrderFrom;
import static se.cygni.texasholdem.player.ActionUtils.safelyChooseInOrderFrom;

public class PostFlopStrategyImpl implements PostFlopStrategy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public Action getPostFlopAction(List<Action> possibleActions, HandRanking ranking, CurrentPlayState playState, GameStatistics gameStatistics) {

        logger.debug("Determine action for hand [{}] with ranking [{}/{}]",
                ranking.getHand(), ranking.getRankingValue(), ranking.getPocketCardsInPokerHand());


        Map<ActionType, Action> actionMap = new HashMap<>();
        for (Action action : possibleActions) {
            actionMap.put(action.getActionType(), action);
        }
        logger.debug("Possible actions [{}]", collectionToCommaDelimitedString(actionMap.keySet()));

        if (ranking.getPocketCardsInPokerHand() == 0) {
            logger.debug("None of pocket cards is part of poker hand. Lets get out of this.");
            return chooseInOrderFrom(actionMap, CHECK, FOLD);

        }

        int calculatedRating = ranking.getRankingValue() + ranking.getPocketCardsInPokerHand();
        if (calculatedRating <= 60 ) {
            logger.debug("Got a calculated rating of [{}] lets be defensive", calculatedRating);
            return chooseInOrderFrom(actionMap, CHECK, FOLD);
        }

        if (calculatedRating > 60 && calculatedRating <= 85) {
            logger.debug("Got a calculated rating of [{}] lets call", calculatedRating);
            return safelyChooseInOrderFrom(actionMap, playState, CALL, CHECK, FOLD);
        }

        if (calculatedRating > 85 && calculatedRating <= 95) {
            logger.debug("Got a calculated rating of [{}] lets raise", calculatedRating);
            return safelyChooseInOrderFrom(actionMap, playState, RAISE, CALL, CHECK, FOLD);
        }

        logger.info("Aggressive: Ratio [{}], players in round [{}], best ratio [{}]", gameStatistics.getChipRatio(), gameStatistics.playersStillInRound(), gameStatistics.getChipRatioBestOpponent());

        // Ok we have a really nice hand, but lets be somewhat careful if we're in FLOP, TURN
        if (of(FLOP, TURN).contains(playState.getCurrentPlayState())) {
            logger.debug("Got a calculated rating of [{}] but only in [{}] so lets just raise", calculatedRating, playState.getCurrentPlayState());
            return chooseInOrderFrom(actionMap, RAISE, CALL, CHECK, FOLD);
        }

        logger.debug("Got a calculated rating of [{}] lets go all in", calculatedRating);
        return chooseInOrderFrom(actionMap, ALL_IN, RAISE, CALL, CHECK, FOLD);
    }
}
