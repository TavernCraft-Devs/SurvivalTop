package tk.taverncraft.survivaltop.inventory.holders;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;

/**
 * Holder for tracking count of inventory items.
 */
public class InventoryHolder {
    private HashMap<Material, Integer> counter;

    /**
     * Constructor for InventoryHolder.
     *
     * @param materials list of inventory materials
     */
    public InventoryHolder(Set<Material> materials) {
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
     * Add count to holder.
     *
     * @param material material to add count for
     * @param amount amount to add
     */
    public void addToHolder(Material material, int amount) {
        counter.merge(material, amount, Integer::sum);
    }
}
