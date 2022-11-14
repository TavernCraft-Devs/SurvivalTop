package tk.taverncraft.survivaltop.inventory.holders;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;

public class InventoryHolder {
    private HashMap<Material, Integer> counter;

    public InventoryHolder(Set<Material> materials) {
        counter = new HashMap<>();
        for (Material material : materials) {
            counter.put(material, 0);
        }
    }

    public HashMap<Material, Integer> getCounter() {
        return this.counter;
    }

    public void addToHolder(Material material, int amount) {
        counter.merge(material, amount, Integer::sum);
    }
}
