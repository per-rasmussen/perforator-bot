package se.cygni.texasholdem.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.client.PlayerClient;
import se.cygni.texasholdem.game.GamePlayer;
import se.cygni.texasholdem.player.utils.PlayState;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;

public class LocalPlayerClient extends PlayerClient {

    private final static Logger LOGGER = LoggerFactory.getLogger(LocalPlayerClient.class);
    private final String host;
    private List<GamePlayer> players;

    /**
     * Overrides some methods because of a bug in @CurrentPlayerState
     *
     * @param player     the Player in game
     * @param serverHost the host name or IP adr to the server
     * @param serverPort the port at which the server is accepting connections
     */
    public LocalPlayerClient(Player player, String serverHost, int serverPort) {
        super(player, serverHost, serverPort);
        this.host = serverHost;
    }

    @Override
    public CurrentPlayState getCurrentPlayState() {
        return new PlayState(getPlayer().getName(), super.getCurrentPlayState(), getPlayers());
    }

    private List<GamePlayer> getPlayers() {
        return this.players;
    }

    public void setPlayers(List<GamePlayer> players) {
        LOGGER.debug("Setting players to [{}]",
                collectionToCommaDelimitedString(
                        players.stream().map(GamePlayer::getName).collect(Collectors.toList())));
        this.players = players;
    }

    public String getHost() {
        return this.host;
    }
}
