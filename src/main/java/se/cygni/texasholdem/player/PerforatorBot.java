package se.cygni.texasholdem.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.communication.message.event.*;
import se.cygni.texasholdem.communication.message.request.ActionRequest;
import se.cygni.texasholdem.game.*;
import se.cygni.texasholdem.player.postflop.*;
import se.cygni.texasholdem.player.preflop.PreFlopStrategy;
import se.cygni.texasholdem.player.preflop.PreFlopStrategyImpl;
import se.cygni.texasholdem.player.stats.GameStatistics;

import java.util.Formatter;

import static java.lang.System.getProperty;
import static java.util.EnumSet.of;
import static se.cygni.texasholdem.game.ActionType.RAISE;
import static se.cygni.texasholdem.game.definitions.PlayState.*;

/**
 * This is the best Cygni-bot of Cygni Virtual Poker Challenge 2014.
 */
public class PerforatorBot implements Player {

    private final static Logger LOG = LoggerFactory.getLogger(PerforatorBot.class);

    private final LocalPlayerClient playerClient;
    private PreFlopStrategy preFlopStrategy;
    private final PostFlopStrategy postFlopStrategy;
    private final HandRankingService handRankingService;
    private GameStatistics gameStatistics;

    /**
     * Default constructor for a Java Poker Bot.
     *
     * @param serverHost IP or hostname to the poker server
     * @param serverPort port at which the poker server listens
     */
    public PerforatorBot(String serverHost, int serverPort) {
        this.playerClient = new LocalPlayerClient(this, serverHost, serverPort);
        this.handRankingService = new HandRankingServiceImpl();
        this.postFlopStrategy = new PostFlopStrategyImpl();
    }

    public void playATrainingGame() throws Exception {
        playerClient.connect();
        final String room = getProperty("room", "TRAINING");
        LOG.info("Trying to start play in [{}]", room);
        playerClient.registerForPlay(Room.valueOf(room));
    }

    /**
     * The main method to start your bot.
     *
     * @param args arguments
     */
    public static void main(String... args) {
        PerforatorBot bot = new PerforatorBot("poker.cygni.se", 4711);

        try {
            bot.playATrainingGame();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The name you choose must be unique, if another connected bot has
     * the same name your bot will be denied connection.
     *
     * @return The name under which this bot will be known
     */
    @Override
    public String getName() {
        return getProperty("botName", "Perforator");
    }

    @Override
    public Action actionRequired(ActionRequest request) {

        CurrentPlayState cps = playerClient.getCurrentPlayState();
        LOG.info("Got an action request in play state [{}]", cps.getCurrentPlayState());

        Action response = request.getPossibleActions().get(0);
        if (cps.getCurrentPlayState() == PRE_FLOP) {
            response = preFlopStrategy.getPreFlopAction(request.getPossibleActions());
        } else if (of(FLOP, TURN, RIVER).contains(cps.getCurrentPlayState())) {
            HandRanking ranking = handRankingService.getRanking(cps.getMyCards(), cps.getCommunityCards());
            response = postFlopStrategy.getPostFlopAction(request.getPossibleActions(), ranking, cps, gameStatistics);

            // Quick fix on raise safe guard
            if (response.getActionType() == RAISE) {
                postFlopStrategy.incrementRaises();
            }
        }

        LOG.info("Round [{}]: I'm going to {} {}. I currently have [{}] chips",
                gameStatistics.getRounds(),
                response.getActionType(),
                response.getAmount() > 0 ? "with " + response.getAmount() : "",
                cps.getMyCurrentChipAmount());

        return response;
    }

    @Override
    public void onPlayIsStarted(final PlayIsStartedEvent event) {
        if (gameStatistics == null) {
            gameStatistics = new GameStatistics(playerClient, getName());
        }
        gameStatistics.incrementRound();
        preFlopStrategy = new PreFlopStrategyImpl(playerClient);
        handRankingService.resetDeck();
        postFlopStrategy.resetRaises();
        playerClient.setPlayers(event.getPlayers());

        LOG.debug("Play is started");
    }

    @Override
    public void onTableChangedStateEvent(TableChangedStateEvent event) {

        LOG.debug("Table changed state: [{}]", event.getState());
        postFlopStrategy.resetRaises();
    }

    @Override
    public void onYouHaveBeenDealtACard(final YouHaveBeenDealtACardEvent event) {

        this.preFlopStrategy.putPocketCard(event.getCard());
        LOG.debug("I, {}, got a card: {}", getName(), event.getCard());
    }

    @Override
    public void onCommunityHasBeenDealtACard(final CommunityHasBeenDealtACardEvent event) {

        LOG.debug("Community got a card: [{}]", event.getCard());
    }

    @Override
    public void onPlayerBetBigBlind(PlayerBetBigBlindEvent event) {

        LOG.debug("{} placed big blind with amount {}", event.getPlayer().getName(), event.getBigBlind());
    }

    @Override
    public void onPlayerBetSmallBlind(PlayerBetSmallBlindEvent event) {

        LOG.debug("{} placed small blind with amount {}", event.getPlayer().getName(), event.getSmallBlind());
    }

    @Override
    public void onPlayerFolded(final PlayerFoldedEvent event) {

        // Check if it's this bot. In that case record that for future
        //event.getPlayer().getName();
        //CurrentPlayState currentPlayState = playerClient.getCurrentPlayState();
        //gameStatistics.recordPlayerFolded(currentPlayState.getPlayers().);


        LOG.debug("{} folded after putting {} in the pot", event.getPlayer().getName(), event.getInvestmentInPot());
    }

    @Override
    public void onPlayerForcedFolded(PlayerForcedFoldedEvent event) {

        LOG.debug("{} was forced to fold after putting {} in the pot because exceeding the time limit", event.getPlayer().getName(), event.getInvestmentInPot());
    }

    @Override
    public void onPlayerCalled(final PlayerCalledEvent event) {

        LOG.debug("{} called with amount {}", event.getPlayer().getName(), event.getCallBet());
    }

    @Override
    public void onPlayerRaised(final PlayerRaisedEvent event) {

        LOG.debug("{} raised with bet {}", event.getPlayer().getName(), event.getRaiseBet());
    }

    @Override
    public void onTableIsDone(TableIsDoneEvent event) {

        LOG.debug("Table is done, I'm leaving the table with ${}", playerClient.getCurrentPlayState().getMyCurrentChipAmount());
        LOG.info("Ending poker session, the last game may be viewed at: http://{}/showgame/table/{}", playerClient.getHost(), playerClient.getCurrentPlayState().getTableId());
    }

    @Override
    public void onPlayerWentAllIn(final PlayerWentAllInEvent event) {

        LOG.debug("{} went all in with amount {}", event.getPlayer().getName(), event.getAllInAmount());
    }

    @Override
    public void onPlayerChecked(final PlayerCheckedEvent event) {


        LOG.debug("{} checked", event.getPlayer().getName());
    }

    @Override
    public void onYouWonAmount(final YouWonAmountEvent event) {

        LOG.debug("I, {}, won: {}", getName(), event.getWonAmount());
    }

    @Override
    public void onShowDown(final ShowDownEvent event) {

        if (!LOG.isInfoEnabled()) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);

        sb.append("ShowDown:\n");

        for (final PlayerShowDown psd : event.getPlayersShowDown()) {
            formatter.format("%-13s won: %6s  hand: %-15s ",
                    psd.getPlayer().getName(),
                    psd.getHand().isFolded() ? "Fold" : psd.getWonAmount(),
                    psd.getHand().getPokerHand().getName());

            sb.append(" cards: | ");
            for (final Card card : psd.getHand().getCards()) {
                formatter.format("%-13s | ", card);
            }
            sb.append("\n");
        }

        LOG.info(sb.toString());
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {

        LOG.debug("Player {} has quit", event.getPlayer());
    }

    @Override
    public void connectionToGameServerLost() {

        LOG.debug("Lost connection to game server, exiting");
        System.exit(0);
    }

    @Override
    public void connectionToGameServerEstablished() {

        LOG.debug("Connection to game server established");
    }

    @Override
    public void serverIsShuttingDown(final ServerIsShuttingDownEvent event) {
        LOG.debug("Server is shutting down");
    }


}
