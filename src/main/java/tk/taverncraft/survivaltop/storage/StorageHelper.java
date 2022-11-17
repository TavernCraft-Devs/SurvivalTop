package tk.taverncraft.survivaltop.storage;

import java.util.ArrayList;

import tk.taverncraft.survivaltop.stats.cache.EntityLeaderboardCache;

/**
 * Interface to determine what type of storage to use. Used to shift work to compile
 * time and decrease work during runtime.
 */
public interface StorageHelper {

    /**
     * Saves information to storage.
     *
     * @param entityLeaderboardCacheList list of entities to store
     */
    void saveToStorage(ArrayList<EntityLeaderboardCache> entityLeaderboardCacheList);
}
