package tk.taverncraft.survivaltop.events.stats;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.InfoMenuOptions;
import tk.taverncraft.survivaltop.gui.StatsMenuOptions;

/**
 * ViewPageEvent checks for when a player clicks on GUI menu.
 */
public class ViewPageEvent implements Listener {
    private final Main main;

    // titles for stats menu
    private final String mainStatsPageTitle;
    private final String subStatsPageBlockTitle;
    private final String subStatsPageSpawnerTitle;
    private final String subStatsPageContainerTitle;
    private final String subStatsPageInventoryTitle;

    // buttons for stats menu
    private final int mainStatsMenuSlot;
    private final int nextStatsPageSlot;
    private final int prevStatsPageSlot;
    private final int blockWealthSlot;
    private final int spawnerWealthSlot;
    private final int containerWealthSlot;
    private final int inventoryWealthSlot;

    // titles for info menu
    private final String mainInfoPageTitle;
    private final String subInfoPageBlockTitle;
    private final String subInfoPageSpawnerTitle;
    private final String subInfoPageContainerTitle;
    private final String subInfoPageInventoryTitle;

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

        mainStatsPageTitle = statsOptions.getMainPageTitle();
        subStatsPageBlockTitle = statsOptions.getSubPageBlockTitle();
        subStatsPageSpawnerTitle = statsOptions.getSubPageSpawnerTitle();
        subStatsPageContainerTitle = statsOptions.getSubPageContainerTitle();
        subStatsPageInventoryTitle = statsOptions.getSubPageInventoryTitle();

        mainStatsMenuSlot = statsOptions.getMainMenuSlot();
        nextStatsPageSlot = statsOptions.getNextPageSlot();
        prevStatsPageSlot = statsOptions.getPrevPageSlot();
        blockWealthSlot = statsOptions.getBlockWealthSlot();
        spawnerWealthSlot = statsOptions.getSpawnerWealthSlot();
        containerWealthSlot = statsOptions.getContainerWealthSlot();
        inventoryWealthSlot = statsOptions.getInventoryWealthSlot();

        InfoMenuOptions infoOptions = main.getGuiManager().getInfoOptions();

        mainInfoPageTitle = infoOptions.getMainPageTitle();
        subInfoPageBlockTitle = infoOptions.getSubPageBlockTitle();
        subInfoPageSpawnerTitle = infoOptions.getSubPageSpawnerTitle();
        subInfoPageContainerTitle = infoOptions.getSubPageContainerTitle();
        subInfoPageInventoryTitle = infoOptions.getSubPageInventoryTitle();

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
        if (title.contains(mainStatsPageTitle)) {
            statsMainPageClickHandler(slot, e);
            return;
        }
        if (title.startsWith(mainInfoPageTitle)) {
            infoMainPageClickHandler(slot, e);
            return;
        }

        // handle stats pagination
        boolean isBlockStatsPage = title.contains(subStatsPageBlockTitle);
        boolean isSpawnerStatsPage = title.contains(subStatsPageSpawnerTitle);
        boolean isContainerStatsPage = title.contains(subStatsPageContainerTitle);
        boolean isInventoryStatsPage = title.contains(subStatsPageInventoryTitle);
        if (isBlockStatsPage || isSpawnerStatsPage || isContainerStatsPage || isInventoryStatsPage) {
            statsSubPageClickHandler(slot, e, isBlockStatsPage, isSpawnerStatsPage,
                    isContainerStatsPage, isInventoryStatsPage);
            return;
        }

        // handle item info pagination
        boolean isBlockInfoPage = title.contains(subInfoPageBlockTitle);
        boolean isSpawnerInfoPage = title.contains(subInfoPageSpawnerTitle);
        boolean isContainerInfoPage = title.contains(subInfoPageContainerTitle);
        boolean isInventoryInfoPage = title.contains(subInfoPageInventoryTitle);
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
            int pageToGo = getPage(e);
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
            int pageToGo = getPage(e);
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
     * Gets the page number to go to.
     *
     * @param e inventory click event
     *
     * @return page number to go to
     */
    private int getPage(InventoryClickEvent e) {
        List<String> itemLore = e.getCurrentItem().getItemMeta().getLore();
        if (itemLore == null) {
            return -1;
        }
        String pageLine = itemLore.get(0);

        // minus 1 because of 0-based indexing
        return Integer.parseInt(pageLine.substring(pageLine.length() - 1)) - 1;
    }
}
