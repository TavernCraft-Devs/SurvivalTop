package tk.taverncraft.survivaltop.gui;

import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;

/**
 * InfoMenuOptions loads all configured menu options for info pages.
 */
public class InfoMenuOptions {
    private final GuiManager guiManager;
    private final int mainPageSize;
    private final int subPageSize;
    private final String mainIdentifier = "§m§i§s§t§o§p";
    private final String blockIdentifier = "§b§i§s§t§o§p";
    private final String spawnerIdentifier = "§s§i§s§t§o§p";
    private final String containerIdentifier = "§c§i§s§t§o§p";
    private final String inventoryIdentifier = "§i§i§s§t§o§p";

    // menu titles
    private final String mainPageTitle;
    private final String subPageBlockTitle;
    private final String subPageSpawnerTitle;
    private final String subPageContainerTitle;
    private final String subPageInventoryTitle;

    // button slots for use in inventory click events
    private final String disabledButtonLore;
    private final int mainMenuSlot;
    private final int nextPageSlot;
    private final int prevPageSlot;
    private final int balInfoSlot;
    private final int blockInfoSlot;
    private final int spawnerInfoSlot;
    private final int containerInfoSlot;
    private final int inventoryInfoSlot;
    private final HashMap<Integer, ItemStack> mainPageBackground = new HashMap<>();
    private final HashMap<Integer, ItemStack> mainPageButtons = new HashMap<>();
    private final HashMap<Integer, ItemStack> subPageBackground = new HashMap<>();
    private final HashMap<Integer, ItemStack> subPageButtons = new HashMap<>();

    // sub page items
    private final String subPageItemName;
    private final List<String> subPageItemLore;

    public InfoMenuOptions(Main main, GuiManager guiManager) {
        this.guiManager = guiManager;
        FileConfiguration config = main.getInfoMenuConfig();
        mainPageSize = config.getInt("main-page-size", 27);
        subPageSize = config.getInt("sub-page-size", 54);
        mainPageTitle = config.getString("main-page-title", "Item Values Info") + mainIdentifier;
        subPageBlockTitle = config.getString("sub-page-block-title", "Block Info") + blockIdentifier ;
        subPageSpawnerTitle = config.getString("sub-page-spawner-title", "Spawner Info") + spawnerIdentifier;
        subPageContainerTitle = config.getString("sub-page-container-title", "Container Info") + containerIdentifier;
        subPageInventoryTitle = config.getString("sub-page-inventory-title", "Inventory Info") + inventoryIdentifier;

        setupMainPageBackground(config);
        setupSubPageBackground(config);
        subPageItemName = config.getString("sub-page-items.name");
        subPageItemLore = config.getStringList("sub-page-items.lore");

        disabledButtonLore = config.getString("disabled-button-lore");
        mainMenuSlot = setUpSubPageButton(config, "main-menu");
        nextPageSlot = setUpSubPageButton(config, "next-page");
        prevPageSlot = setUpSubPageButton(config, "previous-page");
        balInfoSlot = setUpMainPageButton(config, "balance-info", main.getOptions().balIsIncluded());
        blockInfoSlot = setUpMainPageButton(config, "block-info", main.getOptions().landIsIncluded());
        spawnerInfoSlot = setUpMainPageButton(config, "spawner-info", main.getOptions().spawnerIsIncluded());
        containerInfoSlot = setUpMainPageButton(config, "container-info", main.getOptions().containerIsIncluded());
        inventoryInfoSlot = setUpMainPageButton(config, "inventory-info", main.getOptions().inventoryIsIncluded());
    }

    private void setupMainPageBackground(FileConfiguration config) {
        for (String key : config.getConfigurationSection("main-page-background")
            .getKeys(false)) {
            int slot = Integer.parseInt(key);
            Material material = Material.valueOf(config.getString("main-page-background." + key));
            ItemStack itemStack = guiManager.createGuiItem(material, "", false, null);
            mainPageBackground.put(slot, itemStack);
        }
    }

    private void setupSubPageBackground(FileConfiguration config) {
        for (String key : config.getConfigurationSection("sub-page-background")
            .getKeys(false)) {
            int slot = Integer.parseInt(key);
            Material material = Material.valueOf(config.getString("sub-page-background." + key));
            ItemStack itemStack = guiManager.createGuiItem(material, "", false, null);
            subPageBackground.put(slot, itemStack);
        }
    }

    private int setUpMainPageButton(FileConfiguration config, String button, boolean isEnabled) {
        ConfigurationSection configurationSection = config.getConfigurationSection("main-page-buttons." + button);
        int slot = configurationSection.getInt("slot");
        Material material = Material.valueOf(configurationSection.getString("material"));
        String name = configurationSection.getString("name");
        List<String> lore = configurationSection.getStringList("lore");

        ItemStack itemStack = guiManager.createGuiItem(material, name, isEnabled, lore.toArray(new String[0]));
        mainPageButtons.put(slot, itemStack);
        return slot;
    }

    private int setUpSubPageButton(FileConfiguration config, String button) {
        ConfigurationSection configurationSection = config.getConfigurationSection("sub-page-buttons." + button);
        int slot = configurationSection.getInt("slot");
        Material material = Material.valueOf(configurationSection.getString("material"));
        String name = configurationSection.getString("name");
        List<String> lore = configurationSection.getStringList("lore");

        ItemStack itemStack = guiManager.createGuiItem(material, name, false, lore.toArray(new String[0]));
        subPageButtons.put(slot, itemStack);
        return slot;
    }

    public int getMainPageSize() {
        return mainPageSize;
    }

    public int getSubPageSize() {
        return subPageSize;
    }

    public String getMainPageTitle() {
        return mainPageTitle;
    }

    public String getSubPageBlockTitle() {
        return subPageBlockTitle;
    }

    public String getSubPageSpawnerTitle() {
        return subPageSpawnerTitle;
    }

    public String getSubPageContainerTitle() {
        return subPageContainerTitle;
    }

    public String getSubPageInventoryTitle() {
        return subPageInventoryTitle;
    }

    public HashMap<Integer, ItemStack> getMainPageBackground() {
        return mainPageBackground;
    }

    public HashMap<Integer, ItemStack> getMainPageButtons() {
        return mainPageButtons;
    }

    public HashMap<Integer, ItemStack> getSubPageBackground() {
        return subPageBackground;
    }

    public HashMap<Integer, ItemStack> getSubPageButtons() {
        return subPageButtons;
    }

    public String getSubPageItemName() {
        return subPageItemName;
    }

    public List<String> getSubPageItemLore() {
        return subPageItemLore;
    }

    public int getMainMenuSlot() {
        return mainMenuSlot;
    }

    public int getNextPageSlot() {
        return nextPageSlot;
    }

    public int getPrevPageSlot() {
        return prevPageSlot;
    }

    public int getBalInfoSlot() {
        return balInfoSlot;
    }

    public int getBlockInfoSlot() {
        return blockInfoSlot;
    }

    public int getSpawnerInfoSlot() {
        return spawnerInfoSlot;
    }

    public int getContainerInfoSlot() {
        return containerInfoSlot;
    }

    public int getInventoryInfoSlot() {
        return inventoryInfoSlot;
    }

    public String getDisabledButtonLore() {
        return disabledButtonLore;
    }
}
