package tk.taverncraft.survivaltop.ui;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;

public class EntityStatsGui extends GuiHelper {
    Main main;
    private double[] values;

    // list of inventories
    Inventory mainPage;
    ArrayList<Inventory> blockViews;
    ArrayList<Inventory> spawnerViews;
    ArrayList<Inventory> containerViews;

    private final int mainPageSize = 27;

    /**
     * Helps create ui view.
     * @param main plugin class
     * @param uuid uuid of the command sender
     * @param name name of entity to get stats for
     */
    public EntityStatsGui(Main main, UUID uuid, String name, double[] values) {
        this.main = main;
        this.values = values;

        // set up inventories
        name = name + " ";
        setUpMainPage(name);
        HashMap<String, Integer> blockList = main.getLandManager().getSenderBlockForGui(uuid);
        HashMap<String, Integer> spawnerList = main.getLandManager().getSenderSpawnerForGui(uuid);
        HashMap<String, Integer> containerList = main.getLandManager().getSenderContainerForGui(uuid);
        blockViews = prepareViews(blockList, name, "Block Stats");
        spawnerViews = prepareViews(spawnerList, name, "Spawner Stats");
        containerViews = prepareViews(containerList, name, "Container Stats");
    }

    public void setUpMainPage(String entityName) {
        Inventory inv = Bukkit.createInventory(null, this.mainPageSize, entityName + "Wealth Stats" + identifier);
        for (int i = 0; i < mainPageSize; i++) {
            inv.setItem(i, createGuiItem(background, "", false));
        }

        double balValue = values[0];
        double blockValue = values[1];
        double spawnerValue = values[2];
        double containerValue = values[3];
        double landValue = blockValue + spawnerValue + containerValue;
        double totalValue = balValue + landValue;

        inv.setItem(10, createGuiItem(Material.BEACON, "Total Combined Wealth", true,"Total Wealth: " + totalValue));
        if (main.getConfig().getBoolean("include-bal", false)) {
            inv.setItem(11, createGuiItem(Material.EMERALD, "Total Balance Wealth", true,
                "Balance Wealth: " + balValue));
        } else {
            inv.setItem(11, createGuiItem(Material.EMERALD, "Total Balance Wealth", false,
                "Disabled"));
        }
        if (main.getLandManager().getIncludeLand()) {
            inv.setItem(12, createGuiItem(Material.GOLDEN_SHOVEL, "Total Land Wealth", true,
                "Land Wealth: " + landValue));
            inv.setItem(13, createGuiItem(Material.STONE, "Blocks Wealth",
                true, "Block Wealth: " + blockValue, "Click to learn more."));
        } else {
            inv.setItem(12, createGuiItem(Material.GOLDEN_SHOVEL, "Total Land Wealth", false,
                "Disabled"));
            inv.setItem(13, createGuiItem(Material.STONE, "Blocks Wealth", false, "Disabled"));
        }
        if (main.getLandManager().getIncludeSpawners() && main.getLandManager().getIncludeLand()) {
            inv.setItem(14, createGuiItem(Material.SPAWNER, "Spawners Wealth", true, "Spawner " +
                "Wealth: " + spawnerValue, "Click to learn more."));
        } else {
            inv.setItem(14, createGuiItem(Material.SPAWNER, "Spawners Wealth", false, "Disabled"));
        }
        if (main.getLandManager().getIncludeContainers() && main.getLandManager().getIncludeLand()) {
            inv.setItem(15, createGuiItem(Material.CHEST, "Container Wealth", true, "Container " +
                "Wealth: " + containerValue, "Click to learn more."));
        } else {
            inv.setItem(15, createGuiItem(Material.CHEST, "Container Wealth", false, "Disabled"));
        }
        inv.setItem(16, createGuiItem(Material.PLAYER_HEAD, "Inventory Wealth", false, "Coming " +
            "soon."));

        mainPage = inv;
    }

    public Inventory getMainStatsPage() {
        return this.mainPage;
    }

    public Inventory getBlockStatsPage(int pageNum) {
        try {
            return blockViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Inventory getSpawnerStatsPage(int pageNum) {
        try {
            return spawnerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Inventory getContainerStatsPage(int pageNum) {
        try {
            return containerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private ArrayList<Inventory> prepareViews(HashMap<String, Integer> entityList, String entityName, String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeSubPageTemplate(entityName, pageNum, viewType);

        // if no entity, return empty inventory
        if (entityList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int slot = 10;
        for (Map.Entry<String, Integer> map : entityList.entrySet()) {
            String name = map.getKey();
            int quantity = map.getValue();
            double value;
            if (viewType.equals("Block Stats")) {
                value = main.getLandManager().getBlockWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name, false,
                    "Quantity: " + quantity,
                        "Item Worth: " + value, "Total Value: " + value * quantity));
            } else if (viewType.equals("Spawner Stats")) {
                value = main.getLandManager().getSpawnerWorth(name);
                entityView.setItem(slot, createGuiItem(Material.SPAWNER, name,
                    false, "Quantity: " + quantity,
                        "Item Worth: " + value, "Total Value: " + value * quantity));
            } else {
                value = main.getLandManager().getContainerWorth(name);
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name, false,
                    "Quantity: " + quantity,
                        "Item Worth: " + value, "Total Value: " + value * quantity));
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