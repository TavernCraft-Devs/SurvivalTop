package tk.taverncraft.survivaltop.land.operations.holders;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.entity.EntityType;

public class SpawnerHolder {
    private HashMap<EntityType, Integer> counter;

    public SpawnerHolder(Set<EntityType> entityTypes) {
        counter = new HashMap<>();
        for (EntityType entityType : entityTypes) {
            counter.put(entityType, 0);
        }
    }

    public HashMap<EntityType, Integer> getCounter() {
        return this.counter;
    }

    public void addToHolder(EntityType entityType) {
        counter.merge(entityType, 1, Integer::sum);
    }
}
