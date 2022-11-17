package tk.taverncraft.survivaltop.stats.cache;

import java.util.UUID;

/**
 * EntityLeaderboardCache holds all entity information stored after a leaderboard update. It is mainly
 * used for updating the leaderboard, non-realtime stats retrieval and papi placeholders.
 */
public class EntityLeaderboardCache {
    private final UUID UUID;
    private double balWealth = 0;
    private double blockWealth = 0;
    private double spawnerWealth = 0;
    private double containerWealth = 0;
    private double inventoryWealth = 0;

    /**
     * Constructor for EntityLeaderboardCache.
     *
     * @param uuid uuid of entity
     * @param balWealth balance wealth of entity
     */
    public EntityLeaderboardCache(UUID uuid, double balWealth) {
        this.UUID = uuid;
        this.balWealth = balWealth;
    }

    /**
     * Gets the uuid of the entity.
     *
     * @return uuid of entity
     */
    public UUID getUuid() {
        return UUID;
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

    /**
     * Sets the block wealth of an entity.
     *
     * @param newBlockWealth block wealth to set
     */
    public void setBlockWealth(double newBlockWealth) {
        this.blockWealth = newBlockWealth;
    }

    /**
     * Sets the spawner wealth of an entity.
     *
     * @param newSpawnerWealth spawner wealth to set
     */
    public void setSpawnerWealth(double newSpawnerWealth) {
        this.spawnerWealth = newSpawnerWealth;
    }

    /**
     * Sets the container wealth of an entity.
     *
     * @param newContainerWealth container wealth to set
     */
    public void setContainerWealth(double newContainerWealth) {
        this.containerWealth = newContainerWealth;
    }

    /**
     * Sets the inventory wealth of an entity.
     *
     * @param newInventoryWealth inventory wealth to set
     */
    public void setInventoryWealth(double newInventoryWealth) {
        this.inventoryWealth = newInventoryWealth;
    }
}
