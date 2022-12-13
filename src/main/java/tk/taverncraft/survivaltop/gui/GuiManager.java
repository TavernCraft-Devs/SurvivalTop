package tk.taverncraft.survivaltop.gui;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.options.InfoMenuOptions;
import tk.taverncraft.survivaltop.gui.options.StatsMenuOptions;
import tk.taverncraft.survivaltop.gui.types.InfoGui;
import tk.taverncraft.survivaltop.gui.types.StatsGui;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * GuiManager handles all logic related to showing information in a GUI.
 */
public class GuiManager {
    private final Main main;
    private final PermissionsManager permissionsManager;

    // stats and info menu options
    private StatsMenuOptions statsOptions;
    private InfoMenuOptions infoOptions;

    // a single info gui shared by all players since info are the same
    private InfoGui infoGui;

    // map to track current stats gui being viewed by player
    private final ConcurrentHashMap<UUID, String> senderGui = new ConcurrentHashMap<>();

    /**
     * Constructor for GuiManager.
     *
     * @param main plugin class
     */
    public GuiManager(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
        initializeMenuOptions();
    }

    /**
     * Loads all menu options and set common info gui.
     */
    public void initializeMenuOptions() {
        this.statsOptions = new StatsMenuOptions(main);
        this.infoOptions = new InfoMenuOptions(main);
        this.infoGui = infoOptions.createInfoGui();
    }

    /**
     * Returns the stats gui for a given entity.
     *
     * @param name name of entity
     * @param wealthBreakdown wealth breakdown of entity
     * @param blockList count for blocks of entity
     * @param spawnerList count for spawners of entity
     * @param containerList count for containers of entity
     * @param inventoryList count for inventories of entity
     *
     * @return stats gui showing stats for entity
     */
    public StatsGui getStatsGui(String name, HashMap<String, Double> wealthBreakdown,
            HashMap<String, MutableInt> blockList, HashMap<String, MutableInt> spawnerList,
            HashMap<String, MutableInt> containerList, HashMap<String, MutableInt> inventoryList) {

        return statsOptions.createStatsGui(name, wealthBreakdown, blockList, spawnerList,
            containerList, inventoryList);
    }

    /**
     * Special handler to open player inventory gui stats main page when link is clicked.
     *
     * @param uuid uuid of sender
     * @param name of entity
     */
    public void getMainStatsPage(UUID uuid, String name) {
        try {
            senderGui.put(uuid, name);
            StatsGui entityGui = main.getCacheManager().getEntityGui(name);
            if (entityGui != null) {
                Bukkit.getPlayer(uuid).openInventory(entityGui.getMainStatsPage());
            }
        } catch (Exception e) {
            LogManager.error(e.getMessage());
        }
    }

    /**
     * Special handler to open player inventory gui stats main page when returning from subpages.
     *
     * @param uuid uuid of sender
     */
    public void getMainStatsPage(UUID uuid) {
        try {
            String name = senderGui.get(uuid);
            StatsGui entityGui = main.getCacheManager().getEntityGui(name);
            if (entityGui != null) {
                Bukkit.getPlayer(uuid).openInventory(entityGui.getMainStatsPage());
            }
        } catch (Exception e) {
            LogManager.error(e.getMessage());
        }
    }

    /**
     * Retrieves player gui stats block page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing block info for given page
     */
    public Inventory getBlockStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        String entityName = senderGui.get(uuid);
        String senderName = getUpperCaseNameForEntity(uuid);
        if (senderName.equals(entityName)) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getBlockStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getBlockStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Retrieves player gui stats spawner page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing spawner info for given page
     */
    public Inventory getSpawnerStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        String entityName = senderGui.get(uuid);
        String senderName = getUpperCaseNameForEntity(uuid);
        if (senderName.equals(entityName)) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getSpawnerStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getSpawnerStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Retrieves player gui stats container page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing container info for given page
     */
    public Inventory getContainerStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        String entityName = senderGui.get(uuid);
        String senderName = getUpperCaseNameForEntity(uuid);
        if (senderName.equals(entityName)) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getContainerStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getContainerStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Retrieves player gui stats inventory page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing inventory info for given page
     */
    public Inventory getInventoryStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        String entityName = senderGui.get(uuid);
        String senderName = getUpperCaseNameForEntity(uuid);
        if (senderName.equals(entityName)) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getInventoryStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return main.getCacheManager().getEntityGui(entityName).getInventoryStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Gets the main page for info.
     *
     * @return main page of info
     */
    public Inventory getMainInfoPage() {
        return infoGui.getMainInfoPage();
    }

    /**
     * Gets the block info page.
     *
     * @param pageNum page number to show
     * @return inventory showing block page of info
     */
    public Inventory getBlockInfoPage(int pageNum) {
        return infoGui.getBlockInfoPage(pageNum);
    }

    /**
     * Gets the spawner info page.
     *
     * @param pageNum page number to show
     * @return inventory showing spawner page of info
     */
    public Inventory getSpawnerInfoPage(int pageNum) {
        return infoGui.getSpawnerInfoPage(pageNum);
    }

    /**
     * Gets the container info page.
     *
     * @param pageNum page number to show
     * @return inventory showing container page of info
     */
    public Inventory getContainerInfoPage(int pageNum) {
        return infoGui.getContainerInfoPage(pageNum);
    }

    /**
     * Gets the inventory info page.
     *
     * @param pageNum page number to show
     * @return inventory showing inventory page of info
     */
    public Inventory getInventoryInfoPage(int pageNum) {
        return infoGui.getInventoryInfoPage(pageNum);
    }

    /**
     * Gets the options for stats menu to handle inventory click events.
     *
     * @return options for stats menu
     */
    public StatsMenuOptions getStatsOptions() {
        return statsOptions;
    }

    /**
     * Gets the options for info menu to handle inventory click events.
     *
     * @return options for info menu
     */
    public InfoMenuOptions getInfoOptions() {
        return infoOptions;
    }

    /**
     * Gets the upper case name (group or player depending on config) for player viewing stats.
     *
     * @param uuid uuid of player
     *
     * @return upper case name for either the player name or the group of the player
     */
    private String getUpperCaseNameForEntity(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (main.getOptions().groupIsEnabled()) {
            String group = main.getGroupManager().getGroupOfPlayer(player.getName());
            if (group == null) {
                return "";
            }
            return group.toUpperCase();
        } else {
            return player.getName().toUpperCase();
        }
    }
}
