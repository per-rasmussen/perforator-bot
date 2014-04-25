package se.cygni.texasholdem.player.preflop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.client.PlayerClient;
import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.game.ActionType;
import se.cygni.texasholdem.game.Card;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.EnumSet.of;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;
import static se.cygni.texasholdem.game.ActionType.*;
import static se.cygni.texasholdem.player.ActionUtils.chooseInOrderFrom;
import static se.cygni.texasholdem.player.ActionUtils.safelyChooseInOrderFrom;
import static se.cygni.texasholdem.player.preflop.Position.*;
import static se.cygni.texasholdem.player.preflop.StartingHandType.*;

public class PreFlopStrategyImpl implements PreFlopStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreFlopStrategyImpl.class);
    private Card c1;
    private Card c2;
    private final PlayerClient playerClient;
    private StartingHandType type;
    private int raises;

    public PreFlopStrategyImpl(PlayerClient playerClient) {
        this.playerClient = playerClient;
        this.raises = 0;
    }

    @Override
    public void putPocketCard(Card pocketCard) {
        notNull(pocketCard, "'holeCard' cannot be null");
        if (c1 == null) {
            c1 = pocketCard;
        } else {
            c2 = pocketCard;
        }
    }

    @Override
    public Action getPreFlopAction(List<Action> possibleActions) {

        Action preFlopAction = null;

        Map<ActionType, Action> actionMap = new HashMap<>();
        for (Action action : possibleActions) {
            actionMap.put(action.getActionType(), action);
        }
        LOGGER.debug("Possible pre-flop actions [{}]", collectionToCommaDelimitedString(actionMap.keySet()));

        StartingHandType type = getType();
        Position position = getPosition();
        LOGGER.debug("Starting hand type [{}] and position [{}]", type, position);

        if (type == NO_PLAY) {
            LOGGER.debug("Lousy pocket cards. Let's get out of this game. [{}], [{}]", c1, c2);
            preFlopAction = chooseInOrderFrom(actionMap, CHECK, FOLD);
        }

        if (preFlopAction == null && type == ALWAYS_PLAY) {
            LOGGER.debug("Great pocket cards. Lets raise if possible. [{}], [{}]", c1, c2);
            preFlopAction = safelyChooseInOrderFrom(actionMap, playerClient.getCurrentPlayState(), RAISE, CALL, CHECK);
        }

        if (preFlopAction == null && type == MIDDLE_LATE_PLAY && of(MIDDLE, LATE).contains(position)) {
            LOGGER.debug("Good pocket cards [{}], [{}] in position [{}]", c1, c2, position);
            if (raises < 3) {
                preFlopAction = safelyChooseInOrderFrom(actionMap, playerClient.getCurrentPlayState(), RAISE, CALL, CHECK);
            } else {
                preFlopAction = safelyChooseInOrderFrom(actionMap, playerClient.getCurrentPlayState(), CALL, CHECK);
            }
        }

        if (preFlopAction == null && type == ONLY_LATE_PLAY && position == LATE) {
            LOGGER.debug("Decent pocket cards [{}], [{}] in position [{}]", c1, c2, position);
            preFlopAction = safelyChooseInOrderFrom(actionMap, playerClient.getCurrentPlayState(), CALL, CHECK);
        }

        if (preFlopAction == null) {
            LOGGER.debug("Not worth betting on these pocket cards [{}], [{}] in position [{}]", c1, c2, position);
            preFlopAction = chooseInOrderFrom(actionMap, CHECK, FOLD);
        }

        if (preFlopAction.getActionType() == RAISE) {
            raises++;
        }

        return preFlopAction;
    }

    protected Position getPosition() {

        CurrentPlayState playState = playerClient.getCurrentPlayState();
        boolean isDealer = playState.amIDealerPlayer();
        boolean isSmallBlind = playState.amISmallBlindPlayer();
        boolean isBigBlind = playState.amIBigBlindPlayer();

        LOGGER.debug("Number of players: [{}], folded players:[{}], dealer=[{}], sblind=[{}], bblind=[{}]",
                playState.getNumberOfPlayers(), playState.getNumberOfFoldedPlayers(), isDealer, isSmallBlind, isBigBlind);

        if (isDealer) {
            // Special case when I'm the dealer and only 2 players left
            return isSmallBlind ? MIDDLE : LATE;
        }

        if (isSmallBlind || isBigBlind) {
            return EARLY;
        }

        return MIDDLE;
    }

    protected StartingHandType getType() {
        if (this.type == null) {
            this.type = StartingHandType.getGroup(c1, c2);
        }
        return type;
    }
}
