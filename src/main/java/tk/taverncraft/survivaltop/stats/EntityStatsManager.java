package tk.taverncraft.survivaltop.stats;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.EntityStatsGui;
import tk.taverncraft.survivaltop.utils.MessageManager;

/**
 * EntityStatsManager handles all logic for getting entity (player/group) stats but does not
 * store any information. Information storage belongs to the overall ServerStatsManager.
 */
public class EntityStatsManager {
    Main main;

    // prevent stats command spam
    public final HashMap<UUID, Boolean> isCalculatingStats = new HashMap<>();
    public final HashMap<UUID, BukkitTask> statsInitialTask = new HashMap<>();
    public final HashMap<UUID, BukkitTask> statsUiTask = new HashMap<>();

    // uuid is that of the sender
    private HashMap<UUID, EntityStatsGui> senderGui = new HashMap<>();

    /**
     * Constructor for EntityStatsManager.
     */
    public EntityStatsManager(Main main) {
        this.main = main;
    }

    /**
     * Gets the stats of an entity.
     *
     * @param name name of entity
     */
    public void getEntityStats(CommandSender sender, String name) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                calculateEntityStats(sender, name);
            }

        }.runTaskAsynchronously(main);
        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = main.getConsoleUuid();
        }
        statsInitialTask.put(uuid, task);
    }

    /**
     * Calculates the stats of an entity.
     *
     * @param sender user who requested for stats
     * @param name name of entity to get stats for
     */
    private void calculateEntityStats(CommandSender sender, String name) {
        double landWealth = 0;
        double balWealth = 0;
        boolean useGui = main.getConfig().getBoolean("use-gui-stats", true);
        if (main.getConfig().getBoolean("include-land", false)) {
            landWealth = getEntityLandWealth(sender, name);
        }

        if (main.getConfig().getBoolean("include-bal", false)) {
            balWealth = getEntityBalWealth(name);
        }
        final double tempLandWealth = landWealth;
        final double tempBalWealth = balWealth;
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid;
                boolean isPlayer = sender instanceof Player;
                if (isPlayer) {
                    uuid = ((Player) sender).getUniqueId();
                } else {
                    uuid = main.getConsoleUuid();
                }
                double spawnerValue = main.getLandManager().calculateSpawnerWorthForIndividual(uuid, useGui);
                double containerValue = main.getLandManager().calculateContainerWorthForIndividual(uuid, useGui);
                if (useGui && isPlayer) {
                    prepareSenderStatsGui(sender, name, tempBalWealth, tempLandWealth, spawnerValue,
                        containerValue);
                } else {
                    postEntityStatsProcessing(sender, name, null, tempBalWealth, tempLandWealth,
                        spawnerValue, containerValue);
                }
            }
        }.runTask(main);
    }

    private void prepareSenderStatsGui(CommandSender sender, String name, double... values) {
        UUID uuid = ((Player) sender).getUniqueId();
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                EntityStatsGui gui = new EntityStatsGui(main, uuid, name, values);
                senderGui.put(uuid, gui);
                postEntityStatsProcessing(sender, name, gui, values);
            }
        }.runTaskAsynchronously(main);
        this.statsUiTask.put(uuid, task);
    }

    /**
     * Cleans up after an entity's stats has been retrieved. Also updates spawner values if
     * applicable.
     *
     * @param sender sender who checked for stats
     * @param name name of entity to get stats for
     */
    private void postEntityStatsProcessing(CommandSender sender, String name, EntityStatsGui gui,
                                           double... values) {
        if (gui == null) {
            double balValue = values[0];
            double blockValue = values[1];
            double spawnerValue = values[2];
            double containerValue = values[3];
            double landValue = blockValue + spawnerValue + containerValue;
            double totalValue = balValue + landValue;

            String strTotalWealth = String.format("%.02f", totalValue);
            String strBalWealth = String.format("%.02f", balValue);
            String strLandWealth = String.format("%.02f", landValue);
            String strBlockWealth = String.format("%.02f", blockValue);
            String strSpawnerWealth = String.format("%.02f", spawnerValue);
            String strContainerWealth = String.format("%.02f", containerValue);

            MessageManager.sendMessage(sender, "entity-stats",
                    new String[]{"%entity%", "%landwealth%", "%balwealth%", "%totalwealth%", "%blockwealth%", "%spawnerwealth%", "%containerwealth%"},
                    new String[]{name, new BigDecimal(strLandWealth).toPlainString(), new BigDecimal(strBalWealth).toPlainString(),
                    new BigDecimal(strTotalWealth).toPlainString(), new BigDecimal(strBlockWealth).toPlainString(),
                    new BigDecimal(strSpawnerWealth).toPlainString(), new BigDecimal(strContainerWealth).toPlainString()});
        } else {
            TextComponent message = new TextComponent("Click here to view stats!");
            message.setColor(ChatColor.GOLD);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/st openstatsinv"));
            sender.spigot().sendMessage(message);
        }

        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = main.getConsoleUuid();
        }
        main.getLandManager().resetSenderLists(uuid);
        isCalculatingStats.remove(uuid);
        statsInitialTask.remove(uuid);
    }

    private double getEntityLandWealth(CommandSender sender, String name) {
        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = main.getConsoleUuid();
        }

        return main.getLandManager().getLand(uuid, name,
                main.getLandManager().getBlockOperationsForIndividual());
    }

    private double getEntityBalWealth(String name) {
        if (this.main.groupIsEnabled()) {
            try {
                double totalBalance = 0;
                List<OfflinePlayer> offlinePlayers = this.main.getGroupManager().getPlayers(name);
                for (OfflinePlayer offlinePlayer : offlinePlayers) {
                    totalBalance += Main.getEconomy().getBalance(offlinePlayer);
                }
                return totalBalance;
            } catch (NoClassDefFoundError | NullPointerException e) {
                return 0;
            }
        } else {
            try {
                OfflinePlayer player = Bukkit.getOfflinePlayer(name);
                return Main.getEconomy().getBalance(player);
            } catch (NoClassDefFoundError | NullPointerException e) {
                return 0;
            }
        }
    }

    public void setCalculatingStats(CommandSender sender) {
        UUID uuid;
        if (sender instanceof Player) {
            uuid = ((Player) sender).getUniqueId();
        } else {
            uuid = main.getConsoleUuid();
        }
        isCalculatingStats.put(uuid, true);
    }

    public boolean senderHasCalculationInProgress(UUID uuid) {
        if (isCalculatingStats.get(uuid) == null) {
            return false;
        } else {
            return isCalculatingStats.get(uuid);
        }
    }

    /**
     * Disable any existing entity stats calculations.
     */
    public void stopEntityStatsCalculations() {
        for (UUID uuid : isCalculatingStats.keySet()) {
            isCalculatingStats.remove(uuid);
            BukkitTask initialTask = statsInitialTask.get(uuid);
            if (initialTask != null) {
                initialTask.cancel();
            }
            statsInitialTask.remove(uuid);
            BukkitTask finalTask = statsInitialTask.get(uuid);
            if (finalTask != null) {
                finalTask.cancel();
            }
            statsUiTask.remove(uuid);
        }
    }

    public void openMainStatsPage(UUID uuid) {
        try {
            Bukkit.getPlayer(uuid).openInventory(senderGui.get(uuid).getMainStatsPage());
        } catch (Exception e) {
            Bukkit.getConsoleSender().sendMessage(e.getMessage());
        }
    }

    public Inventory getBlockStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getBlockStatsPage(pageNum);
    }

    public Inventory getSpawnerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getSpawnerStatsPage(pageNum);
    }

    public Inventory getContainerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getContainerStatsPage(pageNum);
    }
}


