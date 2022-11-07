package tk.taverncraft.survivaltop.storage;

import java.util.UUID;

import tk.taverncraft.survivaltop.Main;

/**
 * Empty helper for cases where storage is not required.
 */
public class NoneHelper implements StorageHelper {
    Main main;

    public NoneHelper(Main main) {
        this.main = main;
    }

    public void saveToStorage(UUID uuid, double landWealth, double balWealth) {}
}
