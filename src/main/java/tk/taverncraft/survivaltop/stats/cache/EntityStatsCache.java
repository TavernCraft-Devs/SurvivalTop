package tk.taverncraft.survivaltop.stats.cache;

import java.time.Instant;

import tk.taverncraft.survivaltop.ui.EntityStatsGui;

/**
 * Stores cache data for entity if realtime stats is disabled.
 */
public class EntityStatsCache {
    private EntityStatsGui gui;
    private final double balWealth;
    private final double blockWealth;
    private final double spawnerWealth;
    private final double containerWealth;
    private final double inventoryWealth;
    private final long cacheTime;

    public EntityStatsCache(EntityStatsGui gui, double balWealth, double blockWealth,
            double spawnerWealth, double containerWealth, double inventoryWealth) {
        this.gui = gui;
        this.balWealth = balWealth;
        this.blockWealth = blockWealth;
        this.spawnerWealth = spawnerWealth;
        this.containerWealth = containerWealth;
        this.inventoryWealth = inventoryWealth;
        this.cacheTime = Instant.now().getEpochSecond();
    }

    /**
     * Gets the gui of the entity (if applicable).
     *
     * @return gui of entity
     */
    public EntityStatsGui getGui() {
        return this.gui;
    }

    /**
     * Gets the balance of wealth of the entity.
     *
     * @return balance wealth of entity
     */
    public double getBalWealth() {
        return balWealth;
    }

    /**
     * Gets the block wealth of the entity
     *
     * @return block wealth of entity
     */
    public double getBlockWealth() {
        return blockWealth;
    }

    /**
     * Gets spawner wealth of the entity
     *
     * @return spawner wealth of the entity
     */
    public double getSpawnerWealth() {
        return spawnerWealth;
    }

    /**
     * Gets the container wealth of the entity
     *
     * @return container wealth of the entity
     */
    public double getContainerWealth() {
        return containerWealth;
    }

    /**
     * Gets the inventory wealth of the entity
     *
     * @return inventory wealth of the entity
     */
    public double getInventoryWealth() {
        return inventoryWealth;
    }

    /**
     * Gets the land wealth of the entity, calculated by summing up block wealth,
     * spawner wealth and container wealth.
     *
     * @return land wealth of the entity
     */
    public double getLandWealth() {
        return blockWealth + spawnerWealth + containerWealth;
    }

    /**
     * Gets the total wealth of the entity, calculated by summing up bal wealth, land wealth
     * and inv wealth.
     *
     * @return total wealth of the entity
     */
    public Double getTotalWealth() {
        return balWealth + getLandWealth() + inventoryWealth;
    }

    public long getCacheTime() {
        return cacheTime;
    }
}
