package se.cygni.texasholdem.player.preflop;

import org.junit.Test;
import se.cygni.texasholdem.game.GamePlayer;
import se.cygni.texasholdem.player.utils.PlayState;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.cygni.texasholdem.player.preflop.Position.LATE;

public class PositionDeterminatorTest {

    private final static String PERFORATOR = "Perforator";

    @Test
    public void testLatePosition() {


        // fixtures
        PositionDeterminator target = new PositionDeterminator();
        PlayState ps = mock(PlayState.class);
        List<GamePlayer> players = getPlayers(6);
        GamePlayer perforator = new GamePlayer(PERFORATOR, 100);
        players.add(perforator);

        when(ps.getName()).thenReturn(PERFORATOR);
        when(ps.getPlayersInPositionOrder()).thenReturn(players);
        when(ps.getNumberOfPlayers()).thenReturn(players.size());

        // test
        Position position = target.getPosition(ps);

        // verify
        assertEquals(LATE, position);

    }

    private List<GamePlayer> getPlayers(int numberOfPlayers) {

        List<GamePlayer> result = new ArrayList<>();
        for (int i = 0; i < numberOfPlayers; i++) {
            GamePlayer player = new GamePlayer("Player " + numberOfPlayers, 10 +  i);
            result.add(player);
        }

        return result;
    }

}