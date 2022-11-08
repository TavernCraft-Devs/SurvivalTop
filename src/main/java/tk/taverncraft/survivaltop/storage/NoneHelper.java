package tk.taverncraft.survivaltop.storage;

import java.util.UUID;

/**
 * Empty helper class that does nothing for cases where storage is not required.
 */
public class NoneHelper implements StorageHelper {

    public NoneHelper() {}
    public void saveToStorage(UUID uuid, double landWealth, double balWealth) {}
}
