package se.cygni.texasholdem.player.preflop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.game.ActionType;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.GamePlayer;
import se.cygni.texasholdem.player.LocalPlayerClient;
import se.cygni.texasholdem.player.utils.PlayState;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.EnumSet.of;
import static java.util.stream.Collectors.toMap;
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
    private final LocalPlayerClient playerClient;
    private final PositionDeterminator positionDeterminator;
    private StartingHandType type;
    private StealBlindsEvaluator stealBlindsEvaluator;
    private int raises;

    public PreFlopStrategyImpl(LocalPlayerClient playerClient) {
        this.playerClient = playerClient;
        this.raises = 0;
        this.positionDeterminator = new PositionDeterminator();
        this.stealBlindsEvaluator = new StealBlindsEvaluator();
    }

    @Override
    public void putPocketCard(Card pocketCard) {
        notNull(pocketCard, "'pocketCard' cannot be null");
        if (c1 == null) {
            c1 = pocketCard;
        } else {
            c2 = pocketCard;
        }
    }

    @Override
    public Action getPreFlopAction(List<Action> possibleActions) {

        Action preFlopAction = null;

        Map<ActionType, Action> actionMap =
                possibleActions
                        .stream()
                        .collect(toMap(Action::getActionType, a -> a));

        LOGGER.debug("Possible pre-flop actions [{}]", collectionToCommaDelimitedString(actionMap.keySet()));

        PlayState playState = (PlayState) playerClient.getCurrentPlayState();

        if (actionMap.containsKey(CALL)) {
            Action callAction = actionMap.get(CALL);
            long callActionAmount = callAction.getAmount();
            long potSize = playState.getPotTotal();
            double finalPotSize = callActionAmount + potSize;
            LOGGER.info("Call amount is [{}], current pot is [{}], final pot size [{}], pot odds [{}]",
                    callActionAmount, potSize, finalPotSize, callActionAmount / finalPotSize);
        }


        StartingHandType type = getType();
        Position position = getPosition();
        LOGGER.debug("Starting hand type [{}] and position [{}]", type, position);

        int numberOfPlayers = playState.getNumberOfPlayers();

        // OBSERVE This needs to be tweaked for heads up game.
        // OBSERVE This needs to be tweaked for heads up game.
        // OBSERVE This needs to be tweaked for heads up game.
        if (numberOfPlayers == 2 && playState.amIDealerPlayer() &&
                of(ALWAYS_PLAY, MIDDLE_LATE_PLAY, ONLY_LATE_PLAY).contains(type)) {
            LOGGER.debug("Heads up hand type [{}] and position [{}]", type, position);
            if (raises < 4) {
                preFlopAction = chooseInOrderFrom(actionMap, RAISE, CALL, CHECK);
                // FIX THIS
                // FIX THIS
                // FIX THIS
                // FIX THIS
                // preFlopAction.setAmount(preFlopAction.getAmount() * 2);
            } else {
                preFlopAction = chooseInOrderFrom(actionMap, CALL, CHECK);
            }
        }



        // Steal
        boolean stealBlinds = stealBlindsEvaluator.shouldSteal(playState);

        if (preFlopAction == null && type == NO_PLAY) {
            if (stealBlinds && raises < 2) {
                LOGGER.debug("Trying to steal blinds - all others have folded, raises=[{}]", raises);
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, RAISE, CHECK, FOLD);
            } else {
                LOGGER.debug("Lousy pocket cards. Let's get out of this game. [{}], [{}]", c1, c2);
                preFlopAction = chooseInOrderFrom(actionMap, CHECK, FOLD);
            }
        }

        if (preFlopAction == null && type == ALWAYS_PLAY) {
            LOGGER.debug("Great pocket cards. Lets raise if possible. [{}], [{}]", c1, c2);
            if (raises < 6) {
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, RAISE, CALL, CHECK);
                // FIX THIS
                // FIX THIS
                // FIX THIS
                // FIX THIS
                // preFlopAction.setAmount(preFlopAction.getAmount() * 2);
            } else {
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, CALL, CHECK);
            }
        }

        if (preFlopAction == null && type == MIDDLE_LATE_PLAY && of(MIDDLE, LATE).contains(position)) {
            LOGGER.debug("Good pocket cards [{}], [{}] in position [{}]", c1, c2, position);
            if (raises < 3) {
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, RAISE, CALL, CHECK);
            } else {
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, CALL, CHECK);
            }
        }

        if (preFlopAction == null && type == ONLY_LATE_PLAY && position == LATE) {
            if (stealBlinds && raises < 3) {
                LOGGER.debug("Trying to steal blinds - all others have folded, raises=[{}]", raises);
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, RAISE, CALL, CHECK);
            } else {
                LOGGER.debug("Decent pocket cards [{}], [{}] in position [{}]", c1, c2, position);
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, CALL, CHECK);
            }
        }

        if (preFlopAction == null) {
            if (stealBlinds && raises < 2) {
                LOGGER.debug("Trying to steal blinds - all others have folded, raises=[{}]", raises);
                preFlopAction = safelyChooseInOrderFrom(actionMap, playState, RAISE, CHECK, FOLD);
            } else {
                LOGGER.debug("Not worth betting on these pocket cards [{}], [{}] in position [{}]", c1, c2, position);
                preFlopAction = chooseInOrderFrom(actionMap, CHECK, FOLD);
            }
        }

        if (preFlopAction.getActionType() == RAISE) {
            raises++;
        }

        return preFlopAction;
    }


    protected Position getPosition() {

        PlayState playState = (PlayState) playerClient.getCurrentPlayState();
        boolean isDealer = playState.amIDealerPlayer();
        boolean isSmallBlind = playState.amISmallBlindPlayer();
        boolean isBigBlind = playState.amIBigBlindPlayer();

        LOGGER.debug("Player order is [{}] starting w/ SB",
                collectionToCommaDelimitedString(
                        playState.getPlayersInPositionOrder()
                                .stream()
                                .map(GamePlayer::getName)
                                .collect(Collectors.toList())));

        LOGGER.debug("Small blind [{}], Big Blind [{}], Dealer [{}]",
                playState.getSmallBlindPlayer().getName(),
                playState.getBigBlindPlayer().getName(),
                playState.getDealerPlayer().getName());


        LOGGER.debug("Number of players: [{}], folded players:[{}], dealer=[{}], sblind=[{}], bblind=[{}]",
                playState.getNumberOfPlayers(), playState.getNumberOfFoldedPlayers(), isDealer, isSmallBlind, isBigBlind);

        if (playState.getNumberOfPlayers() > 3) {
            return positionDeterminator.getPosition(playState);
        }

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
