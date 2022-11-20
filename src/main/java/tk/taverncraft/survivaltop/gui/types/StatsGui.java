package tk.taverncraft.survivaltop.gui.types;

import java.util.ArrayList;

import org.bukkit.inventory.Inventory;

public class StatsGui {
    // list of inventories
    private Inventory mainPage;
    private ArrayList<Inventory> blockViews;
    private ArrayList<Inventory> spawnerViews;
    private ArrayList<Inventory> containerViews;
    private ArrayList<Inventory> inventoryViews;

    public StatsGui(Inventory mainPage, ArrayList<Inventory> blockViews, ArrayList<Inventory> spawnerViews,
            ArrayList<Inventory> containerViews, ArrayList<Inventory> inventoryViews) {
        this.mainPage = mainPage;
        this.blockViews = blockViews;
        this.spawnerViews = spawnerViews;
        this.containerViews = containerViews;
        this.inventoryViews = inventoryViews;
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
