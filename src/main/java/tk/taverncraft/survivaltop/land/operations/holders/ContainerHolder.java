package tk.taverncraft.survivaltop.land.operations.holders;

import tk.taverncraft.survivaltop.utils.MutableInt;

import java.util.HashMap;
import java.util.Set;

/**
 * Holder for tracking count of container items.
 */
public class ContainerHolder {
    private HashMap<String, MutableInt> counter;

    /**
     * Constructor for ContainerHolder.
     *
     * @param materials list of container materials
     */
    public ContainerHolder(Set<String> materials) {
        counter = new HashMap<>();
        for (String material : materials) {
            counter.put(material, new MutableInt());
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
     * @param material material to add count for
     */
    public void addToHolder(String material) {
        counter.get(material).increment();
    }
}
