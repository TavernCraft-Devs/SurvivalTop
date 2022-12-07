package tk.taverncraft.survivaltop.events.stats;

import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.options.InfoMenuOptions;
import tk.taverncraft.survivaltop.gui.options.StatsMenuOptions;

/**
 * ViewPageEvent checks for when a player clicks on GUI menu.
 */
public class ViewPageEvent implements Listener {
    private final Main main;

    // titles for stats menu
    private final String mainStatsPageIdentifier;
    private final String subStatsPageIdentifier;
    private final String subStatsPageBlockIdentifier;
    private final String subStatsPageSpawnerIdentifier;
    private final String subStatsPageContainerIdentifier;
    private final String subStatsPageInventoryIdentifier;

    // buttons for stats menu
    private final int mainStatsMenuSlot;
    private final int nextStatsPageSlot;
    private final int prevStatsPageSlot;
    private final int blockWealthSlot;
    private final int spawnerWealthSlot;
    private final int containerWealthSlot;
    private final int inventoryWealthSlot;

    // titles for info menu
    private final String mainInfoPageIdentifier;
    private final String subInfoPageIdentifier;
    private final String subInfoPageBlockIdentifier;
    private final String subInfoPageSpawnerIdentifier;
    private final String subInfoPageContainerIdentifier;
    private final String subInfoPageInventoryIdentifier;

    // buttons for info menu
    private final int mainInfoMenuSlot;
    private final int nextInfoPageSlot;
    private final int prevInfoPageSlot;
    private final int blockInfoSlot;
    private final int spawnerInfoSlot;
    private final int containerInfoSlot;
    private final int inventoryInfoSlot;

    // used to identify inventory gui, is there a better alternative that preserves customisation?
    private final String identifier = "§s§t§o§p";

    /**
     * Constructor for ViewPageEvent.
     *
     * @param main plugin class
     */
    public ViewPageEvent(Main main) {
        this.main = main;
        StatsMenuOptions statsOptions = main.getGuiManager().getStatsOptions();

        mainStatsPageIdentifier = statsOptions.getMainPageIdentifier();
        subStatsPageIdentifier = statsOptions.getStatsPageIdentifier();
        subStatsPageBlockIdentifier = statsOptions.getSubPageBlockIdentifier();
        subStatsPageSpawnerIdentifier = statsOptions.getSubPageSpawnerIdentifier();
        subStatsPageContainerIdentifier = statsOptions.getSubPageContainerIdentifier();
        subStatsPageInventoryIdentifier = statsOptions.getSubPageInventoryIdentifier();

        mainStatsMenuSlot = statsOptions.getMainMenuSlot();
        nextStatsPageSlot = statsOptions.getNextPageSlot();
        prevStatsPageSlot = statsOptions.getPrevPageSlot();
        blockWealthSlot = statsOptions.getBlockWealthSlot();
        spawnerWealthSlot = statsOptions.getSpawnerWealthSlot();
        containerWealthSlot = statsOptions.getContainerWealthSlot();
        inventoryWealthSlot = statsOptions.getInventoryWealthSlot();

        InfoMenuOptions infoOptions = main.getGuiManager().getInfoOptions();

        mainInfoPageIdentifier = infoOptions.getMainPageIdentifier();
        subInfoPageIdentifier = infoOptions.getInfoPageIdentifier();
        subInfoPageBlockIdentifier = infoOptions.getSubPageBlockIdentifier();
        subInfoPageSpawnerIdentifier = infoOptions.getSubPageSpawnerIdentifier();
        subInfoPageContainerIdentifier = infoOptions.getSubPageContainerIdentifier();
        subInfoPageInventoryIdentifier = infoOptions.getSubPageInventoryIdentifier();

        mainInfoMenuSlot = infoOptions.getMainMenuSlot();
        nextInfoPageSlot = infoOptions.getNextPageSlot();
        prevInfoPageSlot = infoOptions.getPrevPageSlot();
        blockInfoSlot = infoOptions.getBlockInfoSlot();
        spawnerInfoSlot = infoOptions.getSpawnerInfoSlot();
        containerInfoSlot = infoOptions.getContainerInfoSlot();
        inventoryInfoSlot = infoOptions.getInventoryInfoSlot();
    }

    @EventHandler
    private void onPageItemClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();

        if (!title.endsWith(identifier)) {
            return;
        }

        // cancel events that move items
        InventoryAction action = e.getAction();
        if (checkInventoryEvent(action, e)) {
            e.setCancelled(true);
        }

        int slot = e.getRawSlot();

        if (handleStatsMainPage(slot, e, title)) {
            return;
        }

        if (handleStatsSubPage(slot, e, title)) {
            return;
        }

        if (handleInfoMainPage(slot, e, title)) {
            return;
        }

        handleInfoSubPage(slot, e, title);
    }

    /**
     * Checks if an inventory click event has to be cancelled.
     *
     * @param action inventory action from user
     * @param e inventory click event
     *
     * @return true if event has to be cancelled, false otherwise
     */
    private boolean checkInventoryEvent(InventoryAction action, InventoryClickEvent e) {
        return (action == InventoryAction.PICKUP_ONE
                || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PICKUP_HALF
                || action == InventoryAction.PICKUP_ALL
                || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || action == InventoryAction.CLONE_STACK || action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.SWAP_WITH_CURSOR) || e.isShiftClick();
    }

    /**
     * Handles inventory click events on stats main page.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     * @param title title of inventory
     *
     * @return true if click is handled here, false otherwise
     */
    private boolean handleStatsMainPage(int slot, InventoryClickEvent e, String title) {
        if (!title.endsWith(mainStatsPageIdentifier)) {
            return false;
        }
        Inventory inv = null;
        HumanEntity humanEntity = e.getWhoClicked();
        if (slot == blockWealthSlot && main.getOptions().landIsIncluded()) {
            inv = main.getGuiManager().getBlockStatsPage(humanEntity, 0);
        } else if (slot == spawnerWealthSlot && main.getOptions().spawnerIsIncluded()) {
            inv = main.getGuiManager().getSpawnerStatsPage(humanEntity, 0);
        } else if (slot == containerWealthSlot && main.getOptions().containerIsIncluded()) {
            inv = main.getGuiManager().getContainerStatsPage(humanEntity, 0);
        } else if (slot == inventoryWealthSlot && main.getOptions().inventoryIsIncluded()) {
            inv = main.getGuiManager().getInventoryStatsPage(humanEntity, 0);
        }
        if (inv == null) {
            return false;
        }
        humanEntity.openInventory(inv);
        return true;
    }

    /**
     * Handles inventory click events on stats subpage.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     * @param title title of inventory
     *
     * @return true if click is handled here, false otherwise
     */
    private boolean handleStatsSubPage(int slot, InventoryClickEvent e, String title) {
        if (!title.endsWith(subStatsPageIdentifier)) {
            return false;
        }
        HumanEntity humanEntity = e.getWhoClicked();
        if (slot == prevStatsPageSlot || slot == nextStatsPageSlot) {
            int page = getPageToGoTo(e.getView().getTitle(), slot == nextStatsPageSlot);
            Inventory inv = null;
            if (title.endsWith(subStatsPageBlockIdentifier)) {
                inv = main.getGuiManager().getBlockStatsPage(humanEntity, page);
            } else if (title.endsWith(subStatsPageSpawnerIdentifier)) {
                inv = main.getGuiManager().getSpawnerStatsPage(humanEntity, page);
            } else if (title.endsWith(subStatsPageContainerIdentifier)) {
                inv = main.getGuiManager().getContainerStatsPage(humanEntity, page);
            } else if (title.endsWith(subStatsPageInventoryIdentifier)) {
                inv = main.getGuiManager().getInventoryStatsPage(humanEntity, page);
            }
            if (inv == null) {
                return false;
            }
            humanEntity.openInventory(inv);
            return true;
        }

        if (slot == mainStatsMenuSlot) {
            main.getGuiManager().getMainStatsPage(humanEntity.getUniqueId());
            return true;
        }
        return false;
    }

    /**
     * Handles inventory click events on info main page.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     * @param title title of inventory
     *
     * @return true if click is handled here, false otherwise
     */
    private boolean handleInfoMainPage(int slot, InventoryClickEvent e, String title) {
        if (!title.endsWith(mainInfoPageIdentifier)) {
            return false;
        }
        Inventory inv = null;
        if (slot == blockInfoSlot && main.getOptions().landIsIncluded()) {
            inv = main.getGuiManager().getBlockInfoPage(0);
        } else if (slot == spawnerInfoSlot && main.getOptions().spawnerIsIncluded()) {
            inv = main.getGuiManager().getSpawnerInfoPage(0);
        } else if (slot == containerInfoSlot && main.getOptions().containerIsIncluded()) {
            inv = main.getGuiManager().getContainerInfoPage(0);
        } else if (slot == inventoryInfoSlot && main.getOptions().inventoryIsIncluded()) {
            inv = main.getGuiManager().getInventoryInfoPage(0);
        }
        if (inv == null) {
            return false;
        }
        e.getWhoClicked().openInventory(inv);
        return true;
    }

    /**
     * Handles inventory click events on stats subpage.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     * @param title title of inventory
     *
     * @return true if click is handled here, false otherwise
     */
    private boolean handleInfoSubPage(int slot, InventoryClickEvent e, String title) {
        if (!title.endsWith(subInfoPageIdentifier)) {
            return false;
        }
        if (slot == prevInfoPageSlot || slot == nextInfoPageSlot) {
            int page = getPageToGoTo(e.getView().getTitle(), slot == nextInfoPageSlot);

            Inventory inv = null;
            if (title.endsWith(subInfoPageBlockIdentifier)) {
                inv = main.getGuiManager().getBlockInfoPage(page);
            } else if (title.endsWith(subInfoPageSpawnerIdentifier)) {
                inv = main.getGuiManager().getSpawnerInfoPage(page);
            } else if (title.endsWith(subInfoPageContainerIdentifier)) {
               inv = main.getGuiManager().getContainerInfoPage(page);
            } else if (title.endsWith(subInfoPageInventoryIdentifier)) {
                inv = main.getGuiManager().getInventoryInfoPage(page);
            }
            if (inv == null) {
                return false;
            }
            e.getWhoClicked().openInventory(inv);
            return true;
        }

        if (slot == mainInfoMenuSlot) {
            e.getWhoClicked().openInventory(main.getGuiManager().getMainInfoPage());
            return true;
        }

        return false;
    }

    /**
     * Gets the current page number
     *
     * @param title title of inventory
     * @param isNextPage whether slot clicked is for next page
     *
     * @return current page number
     */
    private int getPageToGoTo(String title, boolean isNextPage) {
        int currPage = Integer.parseInt(title.split("§", 3)[1]);
        return isNextPage ? currPage + 1 : currPage - 1;
    }
}
