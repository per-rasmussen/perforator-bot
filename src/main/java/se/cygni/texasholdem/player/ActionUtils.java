package se.cygni.texasholdem.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.game.ActionType;
import se.cygni.texasholdem.game.GamePlayer;

import java.util.Map;

import static java.util.EnumSet.of;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;
import static se.cygni.texasholdem.game.ActionType.*;

public abstract class ActionUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActionUtils.class);

    public static Action chooseInOrderFrom(Map<ActionType, Action> possibleActions, ActionType... actionTypes) {

        for (ActionType actionType : actionTypes) {
            if (possibleActions.containsKey(actionType)) {
                return possibleActions.get(actionType);
            }
        }
        return null;
    }

    public static Action safelyChooseInOrderFrom(Map<ActionType, Action> possibleActions, CurrentPlayState ps, ActionType... actionTypes) {

        // really defensive.
        for (GamePlayer player : ps.getPlayers()) {
            if (ps.hasPlayerGoneAllIn(player)) {
                LOGGER.debug("Player [{}] has gone all in", player.getName());
                return chooseInOrderFrom(possibleActions, CHECK, FOLD);
            }
        }

        // If no players have gone all-in and the only actions available are [ALL_IN, FOLD], I must be running out of
        // cash. In that case I choose ALL_IN
        if (possibleActions.size() == 2 && of(ALL_IN, FOLD).containsAll(possibleActions.keySet())) {
            LOGGER.debug("Running low on cash -> ALL IN. Available actions [{}]. Chip=[{}], invested=[{}], pot=[{}]",
                    collectionToCommaDelimitedString(possibleActions.keySet()),
                    ps.getMyCurrentChipAmount(),
                    ps.getMyInvestmentInPot(),
                    ps.getPotTotal());
            return possibleActions.get(ALL_IN);
        }

        return chooseInOrderFrom(possibleActions, actionTypes);
    }
}
