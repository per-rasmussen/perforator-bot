package se.cygni.texasholdem.player.stats;

import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.client.PlayerClient;
import se.cygni.texasholdem.game.GamePlayer;

public class GameStatistics {

    private final PlayerClient playerClient;
    private int rounds = -1;
    private final String myName;

    public GameStatistics(PlayerClient playerClient, String myName) {
        this.playerClient = playerClient;
        this.myName = myName;
    }

    public int playersStillInRound() {
        CurrentPlayState cps = playerClient.getCurrentPlayState();
        return cps.getNumberOfPlayers() - cps.getNumberOfFoldedPlayers();
    }

    public long getChipRatio() {
        CurrentPlayState cps = playerClient.getCurrentPlayState();
        return 100 * cps.getMyCurrentChipAmount() / getTotalChips();
    }

    public long getTotalChips() {

        long result = 0;
        CurrentPlayState playState = playerClient.getCurrentPlayState();
        for (GamePlayer gp : playState.getPlayers()) {
            result += gp.getChipCount();
        }
        return result;
    }

    public double getChipRatioBestOpponent() {

        CurrentPlayState cps = playerClient.getCurrentPlayState();
        long bestChips = -1;
        for (GamePlayer gamePlayer : cps.getPlayers()) {
            if (!myName.equals(gamePlayer.getName()) && !cps.hasPlayerFolded(gamePlayer)) {
                if (gamePlayer.getChipCount() > bestChips) {
                    bestChips = gamePlayer.getChipCount();
                }
            }
        }

        return ((double) cps.getMyCurrentChipAmount()) / bestChips;
    }


    public void incrementRound() {
        rounds++;
    }

    public int getRounds() {
        return rounds;
    }
}
