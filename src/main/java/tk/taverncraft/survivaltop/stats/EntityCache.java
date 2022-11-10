package tk.taverncraft.survivaltop.stats;

import java.util.UUID;

public class EntityCache {
    private final UUID UUID;
    private final double BAL_WEALTH;
    private final double BLOCK_WEALTH;
    private final double SPAWNER_WEALTH;
    private final double CONTAINER_WEALTH;
    private final double INVENTORY_WEALTH;

    public EntityCache(UUID uuid, double balWealth, double blockWealth, double invWealth,
                       double spawnerWealth, double containerWealth) {
        this.UUID = uuid;
        this.BAL_WEALTH = balWealth;
        this.BLOCK_WEALTH = blockWealth;
        this.INVENTORY_WEALTH = invWealth;
        this.SPAWNER_WEALTH = spawnerWealth;
        this.CONTAINER_WEALTH = containerWealth;
    }

    public UUID getUuid() {
        return UUID;
    }

    public double getBalWealth() {
        return BAL_WEALTH;
    }

    public double getBlockWealth() {
        return BLOCK_WEALTH;
    }

    public double getSpawnerWealth() {
        return SPAWNER_WEALTH;
    }

    public double getContainerWealth() {
        return CONTAINER_WEALTH;
    }

    public double getInventoryWealth() {
        return INVENTORY_WEALTH;
    }

    public double getLandWealth() {
        return BLOCK_WEALTH + SPAWNER_WEALTH + CONTAINER_WEALTH;
    }

    public Double getTotalWealth() {
        return BAL_WEALTH + getLandWealth() + INVENTORY_WEALTH;
    }

    public EntityCache setSpawnerWealth(double newSpawnerWealth) {
        return new EntityCache(UUID, BAL_WEALTH, BLOCK_WEALTH, INVENTORY_WEALTH,
                newSpawnerWealth, CONTAINER_WEALTH);
    }

    public EntityCache setContainerWealth(double newContainerWealth) {
        return new EntityCache(UUID, BAL_WEALTH, BLOCK_WEALTH, INVENTORY_WEALTH,
            SPAWNER_WEALTH, newContainerWealth);
    }
}
