package tk.taverncraft.survivaltop.land.operations.holders;

import java.util.HashMap;
import java.util.Set;

import org.bukkit.Material;

public class ContainerHolder {
    private HashMap<Material, Integer> counter;

    public ContainerHolder(Set<Material> materials) {
        counter = new HashMap<>();
        for (Material material : materials) {
            counter.put(material, 0);
        }
    }

    public HashMap<Material, Integer> getCounter() {
        return this.counter;
    }

    public void addToHolder(Material material) {
        counter.merge(material, 1, Integer::sum);
    }
}
