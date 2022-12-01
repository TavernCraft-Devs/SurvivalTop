package tk.taverncraft.survivaltop.gui;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.options.InfoMenuOptions;
import tk.taverncraft.survivaltop.gui.options.StatsMenuOptions;
import tk.taverncraft.survivaltop.gui.types.InfoGui;
import tk.taverncraft.survivaltop.gui.types.StatsGui;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.stats.cache.EntityCache;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * GuiManager handles all logic related to showing information in a GUI.
 */
public class GuiManager {
    private final Main main;
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

    public StatsGui getStatsGui(String name, double balWealth, double landWealth, double blockWealth,
            double spawnerWealth, double containerWealth, double inventoryWealth, double totalWealth,
            HashMap<String, MutableInt> blockList, HashMap<String, MutableInt> spawnerList,
            HashMap<String, MutableInt> containerList, HashMap<String, MutableInt> inventoryList) {

        return statsOptions.createStatsGui(name, balWealth, landWealth, blockWealth,
            spawnerWealth, containerWealth, inventoryWealth, totalWealth, blockList, spawnerList,
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
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing block info for given page
     */
    public Inventory getBlockStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getBlockStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats spawner page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing spawner info for given page
     */
    public Inventory getSpawnerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getSpawnerStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats container page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing container info for given page
     */
    public Inventory getContainerStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getContainerStatsPage(pageNum);
    }

    /**
     * Retrieves player inventory gui stats inventory page.
     *
     * @param uuid uuid of sender
     * @param pageNum page number to show
     *
     * @return inventory page containing inventory info for given page
     */
    public Inventory getInventoryStatsPage(UUID uuid, int pageNum) {
        return senderGui.get(uuid).getInventoryStatsPage(pageNum);
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
}
