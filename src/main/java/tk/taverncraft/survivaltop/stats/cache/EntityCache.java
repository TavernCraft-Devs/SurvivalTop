package tk.taverncraft.survivaltop.stats.cache;

import java.util.UUID;

/**
 * EntityCache holds all entity information stored after a leaderboard update. It is mainly
 * used for updating the leaderboard, non-realtime stats retrieval and papi placeholders.
 */
public class EntityCache {
    private final UUID UUID;
    private final double BAL_WEALTH;
    private final double BLOCK_WEALTH;
    private final double SPAWNER_WEALTH;
    private final double CONTAINER_WEALTH;
    private final double INVENTORY_WEALTH;

    /**
     * Constructor for EntityCache.
     *
     * @param uuid uuid of entity
     * @param balWealth balance wealth of entity
     * @param blockWealth block wealth of entity
     * @param invWealth inventory wealth of entity
     * @param spawnerWealth spawner wealth of entity
     * @param containerWealth container wealth of entity
     */
    public EntityCache(UUID uuid, double balWealth, double blockWealth, double invWealth,
            double spawnerWealth, double containerWealth) {
        this.UUID = uuid;
        this.BAL_WEALTH = balWealth;
        this.BLOCK_WEALTH = blockWealth;
        this.INVENTORY_WEALTH = invWealth;
        this.SPAWNER_WEALTH = spawnerWealth;
        this.CONTAINER_WEALTH = containerWealth;
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
        return BAL_WEALTH;
    }

    /**
     * Gets the block wealth of the entity
     *
     * @return block wealth of entity
     */
    public double getBlockWealth() {
        return BLOCK_WEALTH;
    }

    /**
     * Gets spawner wealth of the entity
     *
     * @return spawner wealth of the entity
     */
    public double getSpawnerWealth() {
        return SPAWNER_WEALTH;
    }

    /**
     * Gets the container wealth of the entity
     *
     * @return container wealth of the entity
     */
    public double getContainerWealth() {
        return CONTAINER_WEALTH;
    }

    /**
     * Gets the inventory wealth of the entity
     *
     * @return inventory wealth of the entity
     */
    public double getInventoryWealth() {
        return INVENTORY_WEALTH;
    }

    /**
     * Gets the land wealth of the entity, calculated by summing up block wealth,
     * spawner wealth and container wealth.
     *
     * @return land wealth of the entity
     */
    public double getLandWealth() {
        return BLOCK_WEALTH + SPAWNER_WEALTH + CONTAINER_WEALTH;
    }

    /**
     * Gets the total wealth of the entity, calculated by summing up bal wealth, land wealth
     * and inv wealth.
     *
     * @return total wealth of the entity
     */
    public Double getTotalWealth() {
        return BAL_WEALTH + getLandWealth() + INVENTORY_WEALTH;
    }

    /**
     * Sets the spawner wealth of an entity.
     *
     * @param newSpawnerWealth spawner wealth to set
     *
     * @return new entity cache with the set spawner value
     */
    public EntityCache setSpawnerWealth(double newSpawnerWealth) {
        return new EntityCache(UUID, BAL_WEALTH, BLOCK_WEALTH, INVENTORY_WEALTH,
                newSpawnerWealth, CONTAINER_WEALTH);
    }

    /**
     * Sets the container wealth of an entity.
     *
     * @param newContainerWealth container wealth to set
     *
     * @return new entity cache with the set container value
     */
    public EntityCache setContainerWealth(double newContainerWealth) {
        return new EntityCache(UUID, BAL_WEALTH, BLOCK_WEALTH, INVENTORY_WEALTH,
            SPAWNER_WEALTH, newContainerWealth);
    }
}
