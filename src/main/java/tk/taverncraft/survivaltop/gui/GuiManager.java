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
import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * GuiManager handles all logic related to showing information in a GUI.
 */
public class GuiManager {
    private final Main main;
    private final PermissionsManager permissionsManager;

    private StatsMenuOptions statsOptions;
    private InfoMenuOptions infoOptions;

    private InfoGui infoGui;

    private ConcurrentHashMap<UUID, StatsGui> senderGui = new ConcurrentHashMap<>();

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

    public void initializeMenuOptions() {
        this.statsOptions = new StatsMenuOptions(main);
        this.infoOptions = new InfoMenuOptions(main);
        setInfoGui();
    }

    public void setInfoGui() {
        this.infoGui = infoOptions.createInfoGui();
    }

    public StatsGui getStatsGui(String name, HashMap<String, Double> wealthBreakdown,
            HashMap<String, MutableInt> blockList, HashMap<String, MutableInt> spawnerList,
            HashMap<String, MutableInt> containerList, HashMap<String, MutableInt> inventoryList) {

        return statsOptions.createStatsGui(name, wealthBreakdown, blockList, spawnerList,
            containerList, inventoryList);
    }

    /**
     * Special handler to open player inventory gui stats main page.
     *
     * @param uuid uuid of sender
     */
    public void openMainStatsPage(UUID uuid) {
        try {
            Bukkit.getPlayer(uuid).openInventory(senderGui.get(uuid).getMainStatsPage());
        } catch (Exception e) {
            LogManager.error(e.getMessage());
        }
    }

    /**
     * Retrieves player inventory gui stats block page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing block info for given page
     */
    public Inventory getBlockStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        StatsGui statsGui = senderGui.get(uuid);
        String name = getUpperCaseNameForEntity(uuid);
        if (name.equals(statsGui.getName().toUpperCase())) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return senderGui.get(uuid).getBlockStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return senderGui.get(uuid).getBlockStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Retrieves player inventory gui stats spawner page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing spawner info for given page
     */
    public Inventory getSpawnerStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        StatsGui statsGui = senderGui.get(uuid);
        String name = getUpperCaseNameForEntity(uuid);
        if (name.equals(statsGui.getName().toUpperCase())) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return senderGui.get(uuid).getSpawnerStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return senderGui.get(uuid).getSpawnerStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Retrieves player inventory gui stats container page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing container info for given page
     */
    public Inventory getContainerStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        StatsGui statsGui = senderGui.get(uuid);
        String name = getUpperCaseNameForEntity(uuid);
        if (name.equals(statsGui.getName().toUpperCase())) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return senderGui.get(uuid).getContainerStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return senderGui.get(uuid).getContainerStatsPage(pageNum);
            }
        }
        return null;
    }

    /**
     * Retrieves player inventory gui stats inventory page.
     *
     * @param humanEntity user who clicked the gui
     * @param pageNum page number to show
     *
     * @return inventory page containing inventory info for given page
     */
    public Inventory getInventoryStatsPage(HumanEntity humanEntity, int pageNum) {
        UUID uuid = humanEntity.getUniqueId();
        StatsGui statsGui = senderGui.get(uuid);
        String name = getUpperCaseNameForEntity(uuid);
        if (name.equals(statsGui.getName().toUpperCase())) {
            if (permissionsManager.hasGuiDetailsSelfPerm(humanEntity)) {
                return senderGui.get(uuid).getInventoryStatsPage(pageNum);
            }
        } else {
            if (permissionsManager.hasGuiDetailsOthersPerm(humanEntity)) {
                return senderGui.get(uuid).getInventoryStatsPage(pageNum);
            }
        }
        return null;
    }

    public void setSenderGui(UUID uuid, EntityCache eCache) {
        senderGui.put(uuid, eCache.getGui(main));
    }

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

    public StatsMenuOptions getStatsOptions() {
        return statsOptions;
    }

    public InfoMenuOptions getInfoOptions() {
        return infoOptions;
    }

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
