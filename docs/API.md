# Economy API

This mod exposes an internal API interface at:

`me.styna.privateserver.api.economy.EconomyApi`

Methods:

1. `double getBalance(UUID playerId, String playerName)`
2. `double setBalance(UUID playerId, String playerName, double newBalance)`
3. `double deposit(UUID playerId, String playerName, double amount)`
4. `double withdraw(UUID playerId, String playerName, double amount)`
5. `List<EconomyApi.LeaderEntry> getTopBalances(int limit)`

Implementation access:

1. Runtime singleton:
`PrivateServer.get().getEconomyService()`
2. The service implements `EconomyApi`, so integrations can depend on the interface and cast only if needed.

Notes:

1. Economy data is backed by `config/privateserver/databases/data.sqlite`.
2. Player rows are auto-created with `startingBalance` from `modules/economy.json`.
