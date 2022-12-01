package tk.taverncraft.survivaltop.events.stats;

import org.bukkit.Bukkit;
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

    // used to identify inventory gui, consider a better alternative?
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

        // handle stats page and item info page differently
        int slot = e.getRawSlot();
        if (title.endsWith(mainStatsPageIdentifier)) {
            statsMainPageClickHandler(slot, e);
            return;
        }
        if (title.endsWith(mainInfoPageIdentifier)) {
            infoMainPageClickHandler(slot, e);
            return;
        }

        // handle stats pagination
        boolean isBlockStatsPage = title.endsWith(subStatsPageBlockIdentifier);
        boolean isSpawnerStatsPage = title.endsWith(subStatsPageSpawnerIdentifier);
        boolean isContainerStatsPage = title.endsWith(subStatsPageContainerIdentifier);
        boolean isInventoryStatsPage = title.endsWith(subStatsPageInventoryIdentifier);
        if (isBlockStatsPage || isSpawnerStatsPage || isContainerStatsPage || isInventoryStatsPage) {
            statsSubPageClickHandler(slot, e, isBlockStatsPage, isSpawnerStatsPage,
                    isContainerStatsPage, isInventoryStatsPage);
            return;
        }

        // handle item info pagination
        boolean isBlockInfoPage = title.endsWith(subInfoPageBlockIdentifier);
        boolean isSpawnerInfoPage = title.endsWith(subInfoPageSpawnerIdentifier);
        boolean isContainerInfoPage = title.endsWith(subInfoPageContainerIdentifier);
        boolean isInventoryInfoPage = title.endsWith(subInfoPageInventoryIdentifier);
        if (isBlockInfoPage || isSpawnerInfoPage || isContainerInfoPage || isInventoryInfoPage) {
            infoSubPageClickHandler(slot, e, isBlockInfoPage, isSpawnerInfoPage,
                    isContainerInfoPage, isInventoryInfoPage);
        }
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
     */
    private void statsMainPageClickHandler(int slot, InventoryClickEvent e) {
        Inventory inv = null;
        if (slot == blockWealthSlot) {
            inv = main.getGuiManager().getBlockStatsPage(
                    e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == spawnerWealthSlot) {
            inv = main.getGuiManager().getSpawnerStatsPage(
                    e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == containerWealthSlot) {
            inv = main.getGuiManager().getContainerStatsPage(
                    e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == inventoryWealthSlot) {
            inv = main.getGuiManager().getInventoryStatsPage(
                e.getWhoClicked().getUniqueId(), 0);
        }
        if (inv == null) {
            return;
        }
        e.getWhoClicked().openInventory(inv);
    }

    /**
     * Handles inventory click events on stats subpage.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     * @param isBlockPage boolean indicating if current page is for blocks
     * @param isSpawnerPage boolean indicating if current page is for spawners
     * @param isContainerPage boolean indicating if current page is for containers
     * @param isInventoryPage boolean indicating if current page is for inventories
     */
    private void statsSubPageClickHandler(int slot, InventoryClickEvent e, boolean isBlockPage,
                boolean isSpawnerPage, boolean isContainerPage, boolean isInventoryPage) {
        if (slot == prevStatsPageSlot || slot == nextStatsPageSlot) {
            int currPage = getCurrentPage(e.getView().getTitle());
            int pageToGo = slot == nextStatsPageSlot ? currPage + 1 : currPage - 1;
            if (pageToGo == -1) {
                return;
            }

            Inventory inv = null;
            if (isBlockPage) {
                inv = main.getGuiManager().getBlockStatsPage(
                        e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isSpawnerPage) {
                inv = main.getGuiManager().getSpawnerStatsPage(
                        e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isContainerPage) {
                inv = main.getGuiManager().getContainerStatsPage(
                        e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isInventoryPage) {
                inv = main.getGuiManager().getInventoryStatsPage(
                    e.getWhoClicked().getUniqueId(), pageToGo);
            }
            if (inv == null) {
                return;
            }
            e.getWhoClicked().openInventory(inv);
        }

        if (slot == mainStatsMenuSlot) {
            main.getGuiManager().openMainStatsPage(e.getWhoClicked().getUniqueId());
        }
    }

    /**
     * Handles inventory click events on info main page.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     */
    private void infoMainPageClickHandler(int slot, InventoryClickEvent e) {
        Inventory inv = null;
        if (slot == blockInfoSlot) {
            inv = main.getGuiManager().getBlockInfoPage(0);
        } else if (slot == spawnerInfoSlot) {
            inv = main.getGuiManager().getSpawnerInfoPage(0);
        } else if (slot == containerInfoSlot) {
            inv = main.getGuiManager().getContainerInfoPage(0);
        } else if (slot == inventoryInfoSlot) {
            inv = main.getGuiManager().getInventoryInfoPage(0);
        }
        if (inv == null) {
            return;
        }
        e.getWhoClicked().openInventory(inv);
    }

    /**
     * Handles inventory click events on stats subpage.
     *
     * @param slot slot that user clicked on
     * @param e inventory click event
     * @param isBlockPage boolean indicating if current page is for blocks
     * @param isSpawnerPage boolean indicating if current page is for spawners
     * @param isContainerPage boolean indicating if current page is for containers
     * @param isInventoryPage boolean indicating if current page is for inventories
     */
    private void infoSubPageClickHandler(int slot, InventoryClickEvent e, boolean isBlockPage,
            boolean isSpawnerPage, boolean isContainerPage, boolean isInventoryPage) {
        if (slot == prevInfoPageSlot || slot == nextInfoPageSlot) {
            int currPage = getCurrentPage(e.getView().getTitle());
            int pageToGo = slot == nextInfoPageSlot ? currPage + 1 : currPage - 1;
            if (pageToGo == -1) {
                return;
            }
            
            Inventory inv = null;
            if (isBlockPage) {
                inv = main.getGuiManager().getBlockInfoPage(pageToGo);
            } else if (isSpawnerPage) {
                inv = main.getGuiManager().getSpawnerInfoPage(pageToGo);
            } else if (isContainerPage) {
               inv = main.getGuiManager().getContainerInfoPage(pageToGo);
            } else if (isInventoryPage) {
                inv = main.getGuiManager().getInventoryInfoPage(pageToGo);
            }
            if (inv == null) {
                return;
            }
            e.getWhoClicked().openInventory(inv);
        }

        if (slot == mainInfoMenuSlot) {
            e.getWhoClicked().openInventory(main.getGuiManager().getMainInfoPage());
        }
    }

    /**
     * Gets the current page number
     *
     * @param title title of inventory
     *
     * @return current page number
     */
    private int getCurrentPage(String title) {
        return Integer.parseInt(title.split("§", 3)[1]);
    }
}
