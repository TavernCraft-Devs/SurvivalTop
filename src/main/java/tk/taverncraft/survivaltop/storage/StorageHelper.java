package tk.taverncraft.survivaltop.storage;

import java.util.UUID;

/**
 * Interface to determine what type of storage to use. Used to shift work to compile
 * time and decrease work during runtime.
 */
public interface StorageHelper {

    /**
     * Saves information to storage.
     *
     * @param uuid uuid of entity
     * @param landWealth land wealth of entity
     * @param balWealth bal wealth of entity
     */
    void saveToStorage(UUID uuid, double landWealth, double balWealth);
}
