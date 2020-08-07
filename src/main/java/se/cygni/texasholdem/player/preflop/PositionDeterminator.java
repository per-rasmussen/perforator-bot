package se.cygni.texasholdem.player.preflop;

import se.cygni.texasholdem.game.GamePlayer;
import se.cygni.texasholdem.player.utils.PlayState;

import static java.util.stream.Collectors.toList;
import static se.cygni.texasholdem.player.preflop.Position.*;

public class PositionDeterminator {

    private final static Position[][] POSITIONS = new Position[][]{
            /*0*/ { /* ERROR */ },
            /*1*/ { /* ERROR */ },
            /*2*/ {},
            /*3*/ {},
            /*4*/ {EARLY, EARLY, EARLY, LATE},
            /*5*/ {EARLY, EARLY, EARLY, LATE, LATE},
            /*6*/ {EARLY, EARLY, EARLY, MIDDLE, LATE, LATE},
            /*7*/ {EARLY, EARLY, EARLY, MIDDLE, LATE, LATE, LATE},
            /*8*/ {EARLY, EARLY, EARLY, MIDDLE, MIDDLE, LATE, LATE, LATE},
            /*9*/ {EARLY, EARLY, EARLY, EARLY, MIDDLE, MIDDLE, LATE, LATE, LATE},
            /*10*/ {EARLY, EARLY, EARLY, EARLY, MIDDLE, MIDDLE, MIDDLE, LATE, LATE, LATE},
            /*11*/ {EARLY, EARLY, EARLY, EARLY, MIDDLE, MIDDLE, MIDDLE, MIDDLE, LATE, LATE, LATE}
    };

    public Position getPosition(PlayState ps) {
        int players = ps.getNumberOfPlayers();
        int position = ps.getPlayersInPositionOrder()
                        .stream()
                        .map(GamePlayer::getName)
                        .collect(toList()).indexOf(ps.getName());

        return POSITIONS[players][position];
    }
}
