package tk.taverncraft.survivaltop.events.stats;

import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

import org.bukkit.inventory.Inventory;
import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.InfoGui;

public class ViewPageEvent implements Listener {
    Main main;
    private final String identifier = "§s§u§r§v§t§o§p";

    public ViewPageEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    private void onPageItemClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();

        if (!title.endsWith(identifier)) {
            return;
        }

        int slot = e.getRawSlot();
        InventoryAction action = e.getAction();

        if (checkInventoryEvent(action, e)) {
            e.setCancelled(true);
        }

        if (title.contains("Wealth Stats")) {
            statsMainPageClickHandler(slot, e);
            return;
        }

        if (title.startsWith("Land Calculation Info")) {
            infoMainPageClickHandler(slot, e);
            return;
        }

        boolean isBlockStatsPage = title.contains("Block Stats");
        boolean isSpawnerStatsPage = title.contains("Spawner Stats");
        boolean isContainerStatsPage = title.contains("Container Stats");

        if (isBlockStatsPage || isSpawnerStatsPage || isContainerStatsPage) {
            statsSubPageClickHandler(slot, e, isBlockStatsPage, isSpawnerStatsPage, isContainerStatsPage);
            return;
        }

        boolean isBlockInfoPage = title.contains("Block Info");
        boolean isSpawnerInfoPage = title.contains("Spawner Info");
        boolean isContainerInfoPage = title.contains("Container Info");

        if (isBlockInfoPage || isSpawnerInfoPage || isContainerInfoPage) {
            infoSubPageClickHandler(slot, e, isBlockInfoPage, isSpawnerInfoPage, isContainerInfoPage);
        }
    }

    private boolean checkInventoryEvent(InventoryAction action, InventoryClickEvent e) {
        return (action == InventoryAction.PICKUP_ONE
                || action == InventoryAction.PICKUP_SOME || action == InventoryAction.PICKUP_HALF
                || action == InventoryAction.PICKUP_ALL || action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || action == InventoryAction.CLONE_STACK || action == InventoryAction.HOTBAR_SWAP
                || action == InventoryAction.SWAP_WITH_CURSOR) || e.isShiftClick();
    }

    private void statsMainPageClickHandler(int slot, InventoryClickEvent e) {
        Inventory inv = null;
        if (slot == 13) {
            inv = main.getEntityStatsManager().getBlockStatsPage(e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == 14) {
            inv = main.getEntityStatsManager().getSpawnerStatsPage(e.getWhoClicked().getUniqueId(), 0);
        } else if (slot == 15) {
            inv = main.getEntityStatsManager().getContainerStatsPage(e.getWhoClicked().getUniqueId(), 0);
        }
        if (inv == null) {
            return;
        }
        e.getWhoClicked().openInventory(inv);
    }

    private void statsSubPageClickHandler(int slot, InventoryClickEvent e, boolean isBlockPage, boolean isSpawnerPage,
                                          boolean isContainerPage) {
        if (slot == 47 || slot == 51) {
            int pageToGo = getPage(e);
            if (pageToGo == -1) {
                return;
            }

            Inventory inv = null;
            if (isBlockPage) {
                inv = main.getEntityStatsManager().getBlockStatsPage(e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isSpawnerPage) {
                inv = main.getEntityStatsManager().getSpawnerStatsPage(e.getWhoClicked().getUniqueId(), pageToGo);
            } else if (isContainerPage) {
                inv = main.getEntityStatsManager().getContainerStatsPage(e.getWhoClicked().getUniqueId(), pageToGo);
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

    private void infoMainPageClickHandler(int slot, InventoryClickEvent e) {
        Inventory inv = null;
        if (slot == 12) {
            inv = InfoGui.getBlockInfoPage(0);
        } else if (slot == 13) {
            inv = InfoGui.getSpawnerInfoPage(0);
        } else if (slot == 14) {
            inv = InfoGui.getContainerInfoPage(0);
        }
        if (inv == null) {
            return;
        }
        e.getWhoClicked().openInventory(inv);
    }

    private void infoSubPageClickHandler(int slot, InventoryClickEvent e, boolean isBlockPage, boolean isSpawnerPage,
            boolean isContainerPage) {
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
