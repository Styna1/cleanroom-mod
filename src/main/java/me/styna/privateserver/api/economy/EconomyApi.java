package me.styna.privateserver.api.economy;

import java.util.List;
import java.util.UUID;

public interface EconomyApi {
    double getBalance(UUID playerId, String playerName);

    double setBalance(UUID playerId, String playerName, double newBalance);

    double deposit(UUID playerId, String playerName, double amount);

    double withdraw(UUID playerId, String playerName, double amount);

    List<LeaderEntry> getTopBalances(int limit);

    final class LeaderEntry {
        private final String playerName;
        private final double balance;

        public LeaderEntry(String playerName, double balance) {
            this.playerName = playerName;
            this.balance = balance;
        }

        public String getPlayerName() {
            return playerName;
        }

        public double getBalance() {
            return balance;
        }
    }
}
