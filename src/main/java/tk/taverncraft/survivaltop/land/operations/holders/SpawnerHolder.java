package tk.taverncraft.survivaltop.land.operations.holders;

import tk.taverncraft.survivaltop.utils.MutableInt;

import java.util.HashMap;
import java.util.Set;

/**
 * Holder for tracking count of spawners.
 */
public class SpawnerHolder {
    private final HashMap<String, MutableInt> counter;

    /**
     * Constructor for SpawnerHolder.
     *
     * @param entityTypes list of spawner types
     */
    public SpawnerHolder(Set<String> entityTypes) {
        counter = new HashMap<>();
        for (String entityType : entityTypes) {
            counter.put(entityType, new MutableInt());
        }
    }

    /**
     * Gets the tracking counter.
     *
     * @return counter map
     */
    public HashMap<String, MutableInt> getCounter() {
        return this.counter;
    }

    /**
     * Add 1 count to holder.
     *
     * @param entityType entity type to add count for
     */
    public void addToHolder(String entityType) {
        counter.get(entityType).increment();
    }
}
