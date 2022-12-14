package tk.taverncraft.survivaltop.ui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * EntityStatsGui handles all logic related to showing entity stats in a GUI.
 */
public class EntityStatsGui extends GuiHelper {
    private final Main main;
    private final double[] values;

    // list of inventories
    private Inventory mainPage;
    private ArrayList<Inventory> blockViews;
    private ArrayList<Inventory> spawnerViews;
    private ArrayList<Inventory> containerViews;
    private ArrayList<Inventory> inventoryViews;

    private final int mainPageSize = 27;

    /**
     * Constructor for EntityStatsGui, used in real time stats.
     *
     * @param main plugin class
     * @param uuid uuid of the command sender
     * @param name name of entity to get stats for
     * @param values real time stats values
     */
    public EntityStatsGui(Main main, UUID uuid, String name, double[] values) {
        this.main = main;
        this.values = values;

        // set up inventories
        name = name + " ";
        setUpMainPage(name);

        // todo: cleanup this entire mess during the ui update
        HashMap<String, MutableInt> blockList = main.getEntityStatsManager().getBlocksForGuiStats(uuid);
        HashMap<String, MutableInt> spawnerList =
                main.getEntityStatsManager().getSpawnersForGuiStats(uuid);
        HashMap<String, MutableInt> containerList =
                main.getEntityStatsManager().getContainersForGuiStats(uuid);
        HashMap<String, MutableInt> inventoryList =
                main.getEntityStatsManager().getInventoriesForGuiStats(uuid);

        blockViews = prepareViews(blockList, name, "Block Stats");
        spawnerViews = prepareViews(spawnerList, name, "Spawner Stats");
        containerViews = prepareViews(containerList, name, "Container Stats");
        inventoryViews = prepareViews(inventoryList, name, "Inventory Stats");
    }

    /**
     * Sets up the main page for stats.
     *
     * @param entityName name of entity whose stats is shown
     */
    public void setUpMainPage(String entityName) {
        Inventory inv = Bukkit.createInventory(null, this.mainPageSize,
                entityName + "Wealth Stats" + identifier);
        for (int i = 0; i < mainPageSize; i++) {
            inv.setItem(i, createGuiItem(background, "", false));
        }

        double balValue = values[0];
        double blockValue = values[1];
        double spawnerValue = values[2];
        double containerValue = values[3];
        double inventoryValue = values[4];
        double landValue = blockValue + spawnerValue + containerValue;
        double totalValue = balValue + landValue + inventoryValue;

        // formatting to 2dp
        balValue = new BigDecimal(balValue).setScale(2,
                RoundingMode.CEILING).doubleValue();
        totalValue = new BigDecimal(totalValue).setScale(2,
                RoundingMode.CEILING).doubleValue();


        inv.setItem(10, createGuiItem(Material.BEACON, "Total Combined Wealth",
                true,"Total Wealth: " + totalValue));
        if (main.getOptions().balIsIncluded()) {
            inv.setItem(11, createGuiItem(Material.EMERALD, "Total Balance Wealth",
                    true, "Balance Wealth: " + balValue));
        } else {
            inv.setItem(11, createGuiItem(Material.EMERALD, "Total Balance Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().landIsIncluded()) {
            inv.setItem(12, createGuiItem(Material.GOLDEN_SHOVEL, "Total Land Wealth",
                    true, "Land Wealth: " + landValue));
            inv.setItem(13, createGuiItem(Material.GRASS_BLOCK, "Blocks Wealth",
                true, "Block Wealth: " + blockValue, "Click to learn more."));
        } else {
            inv.setItem(12, createGuiItem(Material.GOLDEN_SHOVEL, "Total Land Wealth",
                    false, "Disabled"));
            inv.setItem(13, createGuiItem(Material.GRASS_BLOCK, "Blocks Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().spawnerIsIncluded() && main.getOptions().landIsIncluded()) {
            inv.setItem(14, createGuiItem(Material.SPAWNER, "Spawners Wealth",
                    true, "Spawner " +
                "Wealth: " + spawnerValue, "Click to learn more."));
        } else {
            inv.setItem(14, createGuiItem(Material.SPAWNER, "Spawners Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().containerIsIncluded() && main.getOptions().landIsIncluded()) {
            inv.setItem(15, createGuiItem(Material.CHEST, "Container Wealth",
                    true, "Container Wealth: " + containerValue,
                    "Click to learn more."));
        } else {
            inv.setItem(15, createGuiItem(Material.CHEST, "Container Wealth",
                    false, "Disabled"));
        }
        if (main.getOptions().inventoryIsIncluded()) {
            inv.setItem(16, createGuiItem(Material.PLAYER_HEAD, "Inventory Wealth",
                true, "Inventory Wealth: " + inventoryValue,
                "Click to learn more."));
        } else {
            inv.setItem(16, createGuiItem(Material.PLAYER_HEAD, "Inventory Wealth",
                false, "Disabled"));
        }

        mainPage = inv;
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

    /**
     * Prepares the inventory views for block, spawner and container.
     *
     * @param materialList list of materials to show in gui
     * @param entityName name of entity whose stats is shown
     * @param viewType type of view (block, spawner or container)
     *
     * @return An array list representing pages of inventory for the view type
     */
    private ArrayList<Inventory> prepareViews(HashMap<String, MutableInt> materialList,
                                              String entityName, String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeSubPageTemplate(entityName, pageNum, viewType);

        // if no entity, return empty inventory
        if (materialList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int slot = 10;
        for (Map.Entry<String, MutableInt> map : materialList.entrySet()) {
            String name = map.getKey();
            int quantity = map.getValue().get();
            if (quantity == 0) {
                continue;
            }
            double value;
            switch (viewType) {
            case "Block Stats":
                value = main.getLandManager().getBlockWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            case "Spawner Stats":
                value = main.getLandManager().getSpawnerWorth(name);
                entityView.setItem(slot, createGuiItem(Material.SPAWNER, name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            case "Container Stats":
                value = main.getLandManager().getContainerWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            default:
                value = main.getInventoryManager().getInventoryItemWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name,
                    false, "Quantity: " + quantity,
                    "Item Worth: " + value, "Total Value: " + value * quantity));
                break;
            }

            slot++;

            // next line
            if (slot == 17 || slot == 26) {
                slot += 2;
            }

            // next page
            if (slot == 35) {
                entityViews.add(entityView);
                pageNum++;
                slot = 10;
                entityView = initializeSubPageTemplate(entityName, pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
    }
}
