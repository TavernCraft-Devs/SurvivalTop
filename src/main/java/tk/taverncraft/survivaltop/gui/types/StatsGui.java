package tk.taverncraft.survivaltop.gui.types;

import java.util.ArrayList;

import org.bukkit.inventory.Inventory;

/**
 * StatsGui handles all logic related to showing stats in a GUI.
 */
public class StatsGui {
    // list of inventories
    private final String name;
    private final Inventory mainPage;
    private final ArrayList<Inventory> blockViews;
    private final ArrayList<Inventory> spawnerViews;
    private final ArrayList<Inventory> containerViews;
    private final ArrayList<Inventory> inventoryViews;

    public StatsGui(String name, Inventory mainPage, ArrayList<Inventory> blockViews,
            ArrayList<Inventory> spawnerViews, ArrayList<Inventory> containerViews,
            ArrayList<Inventory> inventoryViews) {
        this.name = name;
        this.mainPage = mainPage;
        this.blockViews = blockViews;
        this.spawnerViews = spawnerViews;
        this.containerViews = containerViews;
        this.inventoryViews = inventoryViews;
    }

    /**
     * Gets the name of the entity for these stats.
     *
     * @return name of entity
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the main stats page.
     *
     * @return inventory showing main page of stats
     */
    public Inventory getMainStatsPage() {
        return this.mainPage;
    }

    /**
     * Gets the block stats page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing block page of stats
     */
    public Inventory getBlockStatsPage(int pageNum) {
        try {
            return blockViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the spawner stats page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing spawner page of stats
     */
    public Inventory getSpawnerStatsPage(int pageNum) {
        try {
            return spawnerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the container stats page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing container page of stats
     */
    public Inventory getContainerStatsPage(int pageNum) {
        try {
            return containerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the inventory stats page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing inventory page of stats
     */
    public Inventory getInventoryStatsPage(int pageNum) {
        try {
            return inventoryViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
