package se.cygni.texasholdem.player.utils;

import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.GamePlayer;
import se.cygni.texasholdem.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Delegate class of @CurrentPlayState
 *
 */
public class PlayState extends CurrentPlayState {

    private final List<GamePlayer> players;
    private final CurrentPlayState delegate;
    private final String name;

    public PlayState(final String myPlayersName, CurrentPlayState delegate, List<GamePlayer> players) {
        super(myPlayersName); // should not use this part! only delegate.
        this.delegate = delegate;
        this.players = players;
        this.name = myPlayersName;
    }

    /**
     * This will return the correctly ordered list of players. Broken by use of HashSet in @CurrentPlatState
     * @return the correct ordered list
     */
    @Override
    public List<GamePlayer> getPlayers() {
        return this.players;
    }

    public String getName() {
        return name;
    }

    /**
     *  So a call to #getPlayers returning a list in the following order CO, D, SB, BB, UTG, HJ
     *  will return the list in SB, BB, UTG, HJ, CO, D
     *
     * @return list of how the players sit around poker table w/ SB indexed att 0.
     */
    public List<GamePlayer> getPlayersInPositionOrder() {

        List<GamePlayer> result = new ArrayList<>(getPlayers().size());
        List<GamePlayer> tail = new ArrayList<>(getPlayers().size());

        boolean located = false;
        String sBName = getSmallBlindPlayer().getName();
        for (GamePlayer player : players) {
            // locate SB-player and add everything after.
            if (player.getName().equals(sBName) || located) {
                located = true;
                result.add(player);
            } else {
                tail.add(player);
            }
        }

        result.addAll(tail);

        return result;
    }

    @Override
    public Player getPlayerImpl() {
        return delegate.getPlayerImpl();
    }

    @Override
    public long getTableId() {
        return delegate.getTableId();
    }

    @Override
    public List<Card> getMyCards() {
        return delegate.getMyCards();
    }

    @Override
    public List<Card> getCommunityCards() {
        return delegate.getCommunityCards();
    }

    @Override
    public List<Card> getMyCardsAndCommunityCards() {
        return delegate.getMyCardsAndCommunityCards();
    }

    @Override
    public se.cygni.texasholdem.game.definitions.PlayState getCurrentPlayState() {
        return delegate.getCurrentPlayState();
    }

    @Override
    public long getPotTotal() {
        return delegate.getPotTotal();
    }

    @Override
    public long getSmallBlind() {
        return delegate.getSmallBlind();
    }

    @Override
    public long getBigBlind() {
        return delegate.getBigBlind();
    }

    @Override
    public GamePlayer getDealerPlayer() {
        return delegate.getDealerPlayer();
    }

    @Override
    public boolean amIDealerPlayer() {
        return delegate.amIDealerPlayer();
    }

    @Override
    public GamePlayer getSmallBlindPlayer() {
        return delegate.getSmallBlindPlayer();
    }

    @Override
    public boolean amISmallBlindPlayer() {
        return delegate.amISmallBlindPlayer();
    }

    @Override
    public GamePlayer getBigBlindPlayer() {
        return delegate.getBigBlindPlayer();
    }

    @Override
    public boolean amIBigBlindPlayer() {
        return delegate.amIBigBlindPlayer();
    }

    @Override
    public long getMyCurrentChipAmount() {
        return delegate.getMyCurrentChipAmount();
    }

    @Override
    public boolean hasPlayerFolded(GamePlayer player) {
        return delegate.hasPlayerFolded(player);
    }

    @Override
    public boolean haveIFolded() {
        return delegate.haveIFolded();
    }

    @Override
    public boolean hasPlayerGoneAllIn(GamePlayer player) {
        return delegate.hasPlayerGoneAllIn(player);
    }

    @Override
    public boolean haveIGoneAllIn() {
        return delegate.haveIGoneAllIn();
    }

    @Override
    public long getInvestmentInPotFor(GamePlayer player) {
        return delegate.getInvestmentInPotFor(player);
    }

    @Override
    public long getMyInvestmentInPot() {
        return delegate.getMyInvestmentInPot();
    }

    @Override
    public int getNumberOfFoldedPlayers() {
        return delegate.getNumberOfFoldedPlayers();
    }

    @Override
    public int getNumberOfPlayers() {
        return delegate.getNumberOfPlayers();
    }

}
