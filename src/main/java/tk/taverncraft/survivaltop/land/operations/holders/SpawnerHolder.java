package tk.taverncraft.survivaltop.land.operations.holders;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.entity.EntityType;

/**
 * Holder for tracking count of spawners.
 */
public class SpawnerHolder {
    private HashMap<EntityType, Integer> counter;

    /**
     * Constructor for SpawnerHolder.
     *
     * @param entityTypes list of spawner types
     */
    public SpawnerHolder(Set<EntityType> entityTypes) {
        counter = new HashMap<>();
        for (EntityType entityType : entityTypes) {
            counter.put(entityType, 0);
        }
    }

    /**
     * Gets the tracking counter.
     *
     * @return counter map
     */
    public HashMap<EntityType, Integer> getCounter() {
        return this.counter;
    }

    /**
     * Add 1 count to holder.
     *
     * @param entityType entity type to add count for
     */
    public void addToHolder(EntityType entityType) {
        counter.merge(entityType, 1, Integer::sum);
    }
}
