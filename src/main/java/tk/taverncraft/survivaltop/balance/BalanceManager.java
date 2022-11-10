package tk.taverncraft.survivaltop.balance;

import org.bukkit.OfflinePlayer;
import tk.taverncraft.survivaltop.Main;

import java.util.List;

public class BalanceManager {
    private Main main;

    public BalanceManager(Main main) {
        this.main = main;
    }

    public double getBalanceByPlayer(OfflinePlayer player) {
        try {
            return Main.getEconomy().getBalance(player);
        }
         catch (Exception | NoClassDefFoundError e) {
            // vault might throw an error here related to null user, remove when resolved
             return 0;
        }
    }

    public double getBalanceByGroup(String group) {
        try {
            double totalBalance = 0;
            List<OfflinePlayer> offlinePlayers =
                this.main.getGroupManager().getPlayers(group);
            for (OfflinePlayer offlinePlayer : offlinePlayers) {
                totalBalance += Main.getEconomy().getBalance(offlinePlayer);
            }
            return totalBalance;
        } catch (NoClassDefFoundError | NullPointerException e) {
            return 0;
        }
    }
}
