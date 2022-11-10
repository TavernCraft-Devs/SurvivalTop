package tk.taverncraft.survivaltop.storage;

import java.util.ArrayList;

import tk.taverncraft.survivaltop.stats.cache.EntityCache;

/**
 * Interface to determine what type of storage to use. Used to shift work to compile
 * time and decrease work during runtime.
 */
public interface StorageHelper {

    /**
     * Saves information to storage.
     *
     * @param entityCacheList list of entities to store
     */
    void saveToStorage(ArrayList<EntityCache> entityCacheList);
}
