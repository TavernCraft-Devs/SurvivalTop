package tk.taverncraft.survivaltop.balance;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import tk.taverncraft.survivaltop.Main;

/**
 * BalanceManager handles logic for retrieving entity balances.
 */
public class BalanceManager {
    private final Main main;

    /**
     * Constructor for BalanceManager.
     *
     * @param main plugin class
     */
    public BalanceManager(Main main) {
        this.main = main;
    }

    /**
     * Gets the balance of an entity based on name.
     *
     * @param name name of entity to get balance for
     *
     * @return total balance of entity
     */
    public double getBalanceForEntity(String name) {
        if (main.getOptions().groupIsEnabled()) {
            return getBalanceByGroup(name);
        }
        return getBalanceByPlayer(name);
    }

    /**
     * Gets the balance from a single player by name.
     *
     * @param name name of player to get balance for
     *
     * @return total balance of player
     */
    private double getBalanceByPlayer(String name) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        try {
            return Main.getEconomy().getBalance(player);
        }
         catch (Exception | NoClassDefFoundError e) {
            // vault might throw an error here related to null user, removeTask when resolved
             return 0;
        }
    }

    /**
     * Gets the balance from a group by name.
     *
     * @param group name of group to get balance for
     *
     * @return total balance of group
     */
    private double getBalanceByGroup(String group) {
        try {
            double totalBalance = 0;
            List<OfflinePlayer> offlinePlayers = this.main.getGroupManager().getPlayers(group);
            for (OfflinePlayer offlinePlayer : offlinePlayers) {
                totalBalance += Main.getEconomy().getBalance(offlinePlayer);
            }
            return totalBalance;
        } catch (NoClassDefFoundError | NullPointerException e) {
            return 0;
        }
    }
}
