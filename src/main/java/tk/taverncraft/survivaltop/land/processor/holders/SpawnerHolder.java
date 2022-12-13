package tk.taverncraft.survivaltop.land.processor.holders;

import java.util.HashMap;
import java.util.Set;

import tk.taverncraft.survivaltop.utils.types.MutableInt;

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
     * Adds 1 count to holder.
     *
     * @param entityType entity type to addTask count for
     */
    public void addToHolder(String entityType) {
        counter.get(entityType).increment();
    }
}
