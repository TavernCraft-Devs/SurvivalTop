package tk.taverncraft.survivaltop.events.stats;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.InfoGui;

/**
 * ViewPageEvent checks for when a player clicks on GUI menu.
 */
public class ViewPageEvent implements Listener {
    private Main main;

    // used to identify inventory gui, consider a better alternative?
    private final String identifier = "§s§u§r§v§t§o§p";

    /**
     * Constructor for ViewPageEvent.
     *
     * @param main plugin class
     */
    public ViewPageEvent(Main main) {
        this.main = main;
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

        // handle stats page and land info page differently
        int slot = e.getRawSlot();
        if (title.contains("Wealth Stats")) {
            statsMainPageClickHandler(slot, e);
            return;
        }
        if (title.startsWith("Wealth Calculation Info")) {
            infoMainPageClickHandler(slot, e);
            return;
        }

        // handle stats pagination
        boolean isBlockStatsPage = title.contains("Block Stats");
        boolean isSpawnerStatsPage = title.contains("Spawner Stats");
        boolean isContainerStatsPage = title.contains("Container Stats");
        boolean isInventoryStatsPage = title.contains("Inventory Stats");
        if (isBlockStatsPage || isSpawnerStatsPage || isContainerStatsPage || isInventoryStatsPage) {
            statsSubPageClickHandler(slot, e, isBlockStatsPage, isSpawnerStatsPage,
                    isContainerStatsPage, isInventoryStatsPage);
            return;
        }

        // handle land info pagination
        boolean isBlockInfoPage = title.contains("Block Info");
        boolean isSpawnerInfoPage = title.contains("Spawner Info");
        boolean isContainerInfoPage = title.contains("Container Info");
        boolean isInventoryInfoPage = title.contains("Inventory Info");
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
        if (slot == 13) {
            inv = main.getEntityStatsManager().getBlockStatsPage(
                    e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == 14) {
            inv = main.getEntityStatsManager().getSpawnerStatsPage(
                    e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == 15) {
            inv = main.getEntityStatsManager().getContainerStatsPage(
                    e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == 16) {
            inv = main.getEntityStatsManager().getInventoryStatsPage(
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
        if (slot == 47 || slot == 51) {
            int pageToGo = getPage(e);
            if (pageToGo == -1) {
                return;
            }

            Inventory inv = null;
            if (isBlockPage) {
                inv = main.getEntityStatsManager().getBlockStatsPage(
                        e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isSpawnerPage) {
                inv = main.getEntityStatsManager().getSpawnerStatsPage(
                        e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isContainerPage) {
                inv = main.getEntityStatsManager().getContainerStatsPage(
                        e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isInventoryPage) {
                inv = main.getEntityStatsManager().getInventoryStatsPage(
                    e.getWhoClicked().getUniqueId(), pageToGo);
            }
            if (inv == null) {
                return;
            }
            e.getWhoClicked().openInventory(inv);
        }

        if (slot == 49) {
            main.getEntityStatsManager().openMainStatsPage(e.getWhoClicked().getUniqueId());
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
        if (slot == 12) {
            inv = InfoGui.getBlockInfoPage(0);
        } else if (slot == 13) {
            inv = InfoGui.getSpawnerInfoPage(0);
        } else if (slot == 14) {
            inv = InfoGui.getContainerInfoPage(0);
        } else if (slot == 15) {
            inv = InfoGui.getInventoryInfoPage(0);
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
        if (slot == 47 || slot == 51) {
            int pageToGo = getPage(e);
            if (pageToGo == -1) {
                return;
            }
            Inventory inv = null;
            if (isBlockPage) {
                inv = InfoGui.getBlockInfoPage(pageToGo);
            } else if (isSpawnerPage) {
                inv = InfoGui.getSpawnerInfoPage(pageToGo);
            } else if (isContainerPage) {
               inv = InfoGui.getContainerInfoPage(pageToGo);
            } else if (isInventoryPage) {
                inv = InfoGui.getInventoryInfoPage(pageToGo);
            }
            if (inv == null) {
                return;
            }
            e.getWhoClicked().openInventory(inv);
        }

        if (slot == 49) {
            e.getWhoClicked().openInventory(InfoGui.mainPage);
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
        List<String> itemLores = e.getCurrentItem().getItemMeta().getLore();
        if (itemLores == null) {
            return -1;
        }
        String pageLine = itemLores.get(0);

        // minus 1 because of 0-based indexing
        return Integer.parseInt(pageLine.substring(pageLine.length() - 1)) - 1;
    }
}
