package tk.taverncraft.survivaltop.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;

public class InfoGui extends GuiHelper {
    Main main;

    // list of worth values
    private static LinkedHashMap<String, Double> blockList;
    private static LinkedHashMap<String, Double> spawnerList;
    private static LinkedHashMap<String, Double> containerList;

    // list of inventories
    public static Inventory mainPage;
    public static ArrayList<Inventory> blockViews;
    public static ArrayList<Inventory> spawnerViews;
    public static ArrayList<Inventory> containerViews;

    private final int mainPageSize = 27;

    public static boolean isReady;

    /**
     * Helps create ui view.
     * @param main plugin class
     */
    public InfoGui(Main main, LinkedHashMap<String, Double> blockList, LinkedHashMap<String, Double> spawnerList,
            LinkedHashMap<String, Double> containerList) {
        this.main = main;
        InfoGui.blockList = blockList;
        InfoGui.spawnerList = spawnerList;
        InfoGui.containerList = containerList;
        initializeAllPages();
    }

    public void initializeAllPages() {
        isReady = false;
        setUpMainPage();
        blockViews = prepareViews(blockList, "Block Info");
        spawnerViews = prepareViews(spawnerList, "Spawner Info");
        containerViews = prepareViews(containerList, "Container Info");
        isReady = true;
    }

    public void setUpMainPage() {
        Inventory inv = Bukkit.createInventory(null, this.mainPageSize, "Land Calculation Info" + identifier);
        for (int i = 0; i < mainPageSize; i++) {
            inv.setItem(i, createGuiItem(background, " ", false));
        }

        inv.setItem(12, createGuiItem(Material.STONE, "Block Info", false,"Click to learn more."));
        inv.setItem(13, createGuiItem(Material.SPAWNER, "Spawner Info", false,"Click to learn " +
            "more."));
        inv.setItem(14, createGuiItem(Material.CHEST, "Container Info", false,"Click to learn " +
            "more."));

        mainPage = inv;
    }

    private ArrayList<Inventory> prepareViews(LinkedHashMap<String, Double> entityList, String viewType) {
        ArrayList<Inventory> entityViews = new ArrayList<>();
        int pageNum = 1;
        Inventory entityView = initializeSubPageTemplate("", pageNum, viewType);

        // if no entity, return empty inventory
        if (entityList == null ) {
            entityViews.add(entityView);
            return entityViews;
        }

        int slot = 10;
        for (Map.Entry<String, Double> map : entityList.entrySet()) {
            String name = map.getKey();
            double value = map.getValue();
            if (viewType.equals("Spawner Info")) {
                entityView.setItem(slot, createGuiItem(Material.SPAWNER, name,
                    false,"Item Worth: " + value));
            } else {
                entityView.setItem(slot, createGuiItem(Material.getMaterial(name), name, false,
                    "Item Worth: " + value));
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
                entityView = initializeSubPageTemplate("", pageNum, viewType);
            }
        }
        entityViews.add(entityView);

        return entityViews;
    }

    public static Inventory getBlockInfoPage(int pageNum) {
        try {
            return blockViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static Inventory getSpawnerInfoPage(int pageNum) {
        try {
            return spawnerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public static Inventory getContainerInfoPage(int pageNum) {
        try {
            return containerViews.get(pageNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
}
