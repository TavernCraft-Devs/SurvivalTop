package tk.taverncraft.survivaltop.land.operations.holders;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;

/**
 * Holder for tracking count of blocks.
 */
public class BlockHolder {
    private HashMap<Material, Integer> counter;

    /**
     * Constructor for BlockHolder.
     *
     * @param materials list of block materials
     */
    public BlockHolder(Set<Material> materials) {
        counter = new HashMap<>();
        for (Material material : materials) {
            counter.put(material, 0);
        }
    }

    /**
     * Gets the tracking counter.
     *
     * @return counter map
     */
    public HashMap<Material, Integer> getCounter() {
        return this.counter;
    }

    /**
     * Add 1 count to holder.
     *
     * @param material material to add count for
     */
    public void addToHolder(Material material) {
        counter.merge(material, 1, Integer::sum);
    }
}
