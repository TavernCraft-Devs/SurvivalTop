package tk.taverncraft.survivaltop.gui.types;

import java.util.ArrayList;

import org.bukkit.inventory.Inventory;

/**
 * InfoGui handles all logic related to showing item info in a GUI.
 */
public class InfoGui {

    // list of inventories
    public Inventory mainPage;
    public ArrayList<Inventory> blockViews;
    public ArrayList<Inventory> spawnerViews;
    public ArrayList<Inventory> containerViews;
    public ArrayList<Inventory> inventoryViews;

    /**
     * Constructor for InfoGui.
     */
    public InfoGui(Inventory mainPage, ArrayList<Inventory> blockViews,
            ArrayList<Inventory> spawnerViews, ArrayList<Inventory> containerViews,
            ArrayList<Inventory> inventoryViews) {
        this.mainPage = mainPage;
        this.blockViews = blockViews;
        this.spawnerViews = spawnerViews;
        this.containerViews = containerViews;
        this.inventoryViews = inventoryViews;
    }

    /**
     * Gets the main page of info gui.
     *
     * @return main page of info gui
     */
    public Inventory getMainInfoPage() {
        return mainPage;
    }

    /**
     * Gets the block info page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing block page of info
     */
    public Inventory getBlockInfoPage(int pageNum) {
        try {
            return blockViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the spawner info page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing spawner page of info
     */
    public Inventory getSpawnerInfoPage(int pageNum) {
        try {
            return spawnerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the container info page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing container page of info
     */
    public Inventory getContainerInfoPage(int pageNum) {
        try {
            return containerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Gets the inventory info page.
     *
     * @param pageNum page number to show
     *
     * @return inventory showing inventory page of info
     */
    public Inventory getInventoryInfoPage(int pageNum) {
        try {
            return inventoryViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
