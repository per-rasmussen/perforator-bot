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
import static se.cygni.texasholdem.game.definitions.PokerHand.FOUR_OF_A_KIND;
import static se.cygni.texasholdem.game.definitions.PokerHand.ROYAL_FLUSH;
import static se.cygni.texasholdem.player.ActionUtils.chooseInOrderFrom;
import static se.cygni.texasholdem.player.ActionUtils.safelyChooseInOrderFrom;

public class PostFlopStrategyImpl implements PostFlopStrategy {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private int raises = 0;

    @Override
    public Action getPostFlopAction(List<Action> possibleActions, HandRanking ranking, CurrentPlayState playState, GameStatistics gameStatistics) {

        int pocketCardsInPokerHand = ranking.getPocketCardsInPokerHand();
        logger.debug("Determine action for hand [{}] with ranking [{}/{}]",
                ranking.getHand(), ranking.getRankingValue(), pocketCardsInPokerHand);


        Map<ActionType, Action> actionMap = new HashMap<>();
        for (Action action : possibleActions) {
            actionMap.put(action.getActionType(), action);
        }
        logger.debug("Possible actions [{}]", collectionToCommaDelimitedString(actionMap.keySet()));

        logger.debug("My chip count [{}] and invested in pot [{}], total pot [{}]",
                playState.getMyCurrentChipAmount(),
                playState.getMyInvestmentInPot(),
                playState.getPotTotal());

        // check obvious wins.
        if (of(ROYAL_FLUSH, FOUR_OF_A_KIND).contains(ranking.getHand().getPokerHand())) {
            logger.debug("Unbeatable hand. Let's raise if possible otherwise go all-in");
            Action action = chooseInOrderFrom(actionMap, RAISE, ALL_IN, CALL, CHECK, FOLD);
            if (action.getActionType() == RAISE) {
                action.setAmount(action.getAmount() * 2);
            }
            return action;
        }

        // Läs på om "Effective stack"

        // QUICKFIX remove RAISE as action if we've raised 5 times
        if (raises > 4) {
            logger.debug("Already raised [{}] times. Removing RAISE as possibility.", raises);
            actionMap.remove(RAISE);
        }

        if (pocketCardsInPokerHand == 0) {
            logger.debug("None of pocket cards is part of poker hand. Lets get out of this.");
            return chooseInOrderFrom(actionMap, CHECK, FOLD);
        }

        int calculatedRating = ranking.getRankingValue() + (pocketCardsInPokerHand * pocketCardsInPokerHand);
        long chipRatio = gameStatistics.getChipRatio();
        if (calculatedRating <= 60) {
            if (chipRatio < 50) {
                logger.debug("Got a calculated rating of [{}] lets be defensive. Ratio [{}]",
                        calculatedRating, chipRatio);
                return chooseInOrderFrom(actionMap, CHECK, FOLD);
            } else {
                logger.debug("Got a calculated rating of [{}] be defensive but call if possible. Ratio [{}]",
                        calculatedRating, chipRatio);
                return chooseInOrderFrom(actionMap, CALL, RAISE, CHECK, FOLD);
            }
        }

        if (calculatedRating <= 85) {
            logger.debug("Got a calculated rating of [{}] lets call", calculatedRating);
            return safelyChooseInOrderFrom(actionMap, playState, CALL, CHECK, FOLD);
        }

        if (calculatedRating <= 95) {
            logger.debug("Got a calculated rating of [{}] lets raise", calculatedRating);
            return safelyChooseInOrderFrom(actionMap, playState, RAISE, CALL, CHECK, FOLD);
        }

        logger.info("Aggressive: Ratio [{}], players in round [{}], best ratio [{}]", chipRatio, gameStatistics.playersStillInRound(), gameStatistics.getChipRatioBestOpponent());

        // Ok we have a really nice hand, but lets be somewhat careful if we're in FLOP, TURN
        if (of(FLOP, TURN).contains(playState.getCurrentPlayState())) {

            // RAISE if i can, otherwise call
            if (actionMap.containsKey(RAISE)) {
                logger.debug("Got a calculated rating of [{}] but only in [{}] so lets just raise", calculatedRating, playState.getCurrentPlayState());
                return chooseInOrderFrom(actionMap, RAISE, CALL, CHECK, FOLD);
            } else {
                logger.debug("Got a calculated rating of [{}] but only in [{}] so lets just call", calculatedRating, playState.getCurrentPlayState());
                return chooseInOrderFrom(actionMap, CALL, CHECK, FOLD);
            }
        }

        logger.debug("Got a calculated rating of [{}] lets go all in", calculatedRating);
        return chooseInOrderFrom(actionMap, ALL_IN, RAISE, CALL, CHECK, FOLD);
    }

    @Override
    public void incrementRaises() {
        raises++;
    }

    @Override
    public void resetRaises() {
        raises = 0;
    }
}
