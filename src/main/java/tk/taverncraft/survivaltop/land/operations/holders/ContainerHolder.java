package tk.taverncraft.survivaltop.land.operations.holders;

import java.util.HashMap;
import java.util.Set;

import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Holder for tracking count of container items.
 */
public class ContainerHolder {
    private final HashMap<String, MutableInt> counter;

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
     * Adds 1 count to holder.
     *
     * @param material material to add count for
     */
    public void addToHolder(String material) {
        counter.get(material).increment();
    }
}
