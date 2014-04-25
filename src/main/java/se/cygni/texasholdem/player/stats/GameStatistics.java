package se.cygni.texasholdem.player.stats;

import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.client.PlayerClient;
import se.cygni.texasholdem.game.GamePlayer;

public class GameStatistics {

    private final PlayerClient playerClient;
    private final long totalChips;
    private int rounds;
    private String myName;

    public GameStatistics(PlayerClient playerClient, String myName) {
        this.playerClient = playerClient;
        CurrentPlayState cps = playerClient.getCurrentPlayState();
        this.totalChips = cps.getMyCurrentChipAmount() * cps.getNumberOfPlayers();
        this.myName = myName;
    }

    public int playersStillInRound() {
        CurrentPlayState cps = playerClient.getCurrentPlayState();
        return cps.getNumberOfPlayers() - cps.getNumberOfFoldedPlayers();
    }

    public double getChipRatio() {
        CurrentPlayState cps = playerClient.getCurrentPlayState();
        return ((double) cps.getMyCurrentChipAmount()) / totalChips;
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
