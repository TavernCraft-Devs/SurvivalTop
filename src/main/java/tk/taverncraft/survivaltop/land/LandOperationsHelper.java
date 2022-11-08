package tk.taverncraft.survivaltop.land;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.UUID;
import java.util.Set;
import java.util.EnumSet;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.ui.InfoGui;

/**
 * Helper function for loading the logic of calculations.
 */
public class LandOperationsHelper {
    Main main;
    LandManager landManager;
    private LinkedHashMap<String, Double> materialWorth = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> spawnerWorth = new LinkedHashMap<>();
    private LinkedHashMap<String, Double> containerWorth = new LinkedHashMap<>();
    private final Set<Material> containerTypes = EnumSet.of(
        Material.CHEST,
        Material.DROPPER,
        Material.HOPPER,
        Material.DISPENSER,
        Material.TRAPPED_CHEST
    );

    /**
     * Constructor for LandOperationsHelper.
     *
     * @param main plugin class
     * @param landManager the manager class for land
     */
    public LandOperationsHelper(Main main, LandManager landManager) {
        this.main = main;
        this.landManager = landManager;
        initializeWorth();
        initializeOperations();
    }

    /**
     * Initializes values of blocks/spawners.
     */
    public void initializeWorth() {
        this.loadMaterialWorth();
        this.loadSpawnerWorth();
        this.loadContainerWorth();

        // load worth values into info gui
        new InfoGui(materialWorth, spawnerWorth, containerWorth);
    }

    /**
     * Initialize operations to be included.
     */
    public void initializeOperations() {
        ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForAll = new ArrayList<>();
        ArrayList<BiFunction<UUID, Block, Double>> blockOperationsForIndividual = new ArrayList<>();

        if (landManager.getIncludeLand()) {
            blockOperationsForAll.add(calculateBlockForAll);
            blockOperationsForIndividual.add(calculateBlockForIndividual);
            if (landManager.getIncludeSpawners()) {
                blockOperationsForAll.add(calculateSpawnerForAll);
                blockOperationsForIndividual.add(calculateSpawnerForIndividual);
            }

            if (landManager.getIncludeContainers()) {
                blockOperationsForAll.add(calculateContainerForAll);
                blockOperationsForIndividual.add(calculateContainerForIndividual);
            }
        }

        landManager.setAllOperations(blockOperationsForAll, blockOperationsForIndividual);
    }

    /**
     * Resets and loads all block values.
     */
    private void loadMaterialWorth() {
        materialWorth = new LinkedHashMap<>();
        for (String key : main.getBlocksConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null || !material.isBlock() || !material.isSolid()) {
                    continue;
                }
                materialWorth.put(key.toUpperCase(), main.getBlocksConfig().getDouble(key));
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }
        }
    }

    /**
     * Resets and loads all spawner values.
     */
    private void loadSpawnerWorth() {
        spawnerWorth = new LinkedHashMap<>();
        for (String key : main.getSpawnersConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                EntityType entityType = EntityType.fromName(key);
                if (entityType == null) {
                    continue;
                }
                spawnerWorth.put(key.toUpperCase(), main.getSpawnersConfig().getDouble(key));
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }
        }
    }

    /**
     * Resets and loads all container values.
     */
    private void loadContainerWorth() {
        containerWorth = new LinkedHashMap<>();
        for (String key : main.getContainersConfig().getConfigurationSection("")
                .getKeys(false)) {
            try {
                Material material = Material.getMaterial(key);
                if (material == null) {
                    continue;
                }
                containerWorth.put(key.toUpperCase(), main.getContainersConfig().getDouble(key));
            } catch (Exception e) {
                Bukkit.getConsoleSender().sendMessage(e.getMessage());
            }
        }
    }

    /**
     * Process the worth of spawners (for all players).
     *
     * @param blocks list of spawner blocks
     *
     * @return double value representing total worth of spawners
     */
    public double processSpawnerWorthForAll(ArrayList<Block> blocks) {
        if (blocks == null) {
            return 0;
        }
        double totalSpawnerWorth = 0;
        for (Block block : blocks) {
            try {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType mobType = spawner.getSpawnedType();
                String mobName = mobType.getName();
                if (mobName != null) {
                    Double value = spawnerWorth.get(mobName.toUpperCase());
                    if (value == null) {
                        value = 0.0;
                    }
                    totalSpawnerWorth += value;
                }
            } catch (ClassCastException e) {
                // error thrown if player breaks spawner just as calculation is taking place
            }
        }
        return totalSpawnerWorth;
    }

    /**
     * Process the worth of container items (for all players).
     *
     * @param blocks list of containers
     *
     * @return double value representing total worth of container items
     */
    public double processContainerWorthForAll(ArrayList<Block> blocks) {
        if (blocks == null) {
            return 0;
        }
        double totalContainerWorth = 0;
        for (Block block : blocks) {
            BlockState blockstate = block.getState();
            Inventory inventory;
            if (blockstate instanceof Chest) {
                Chest chest = (Chest) blockstate;
                inventory = chest.getBlockInventory();
            } else {
                InventoryHolder inventoryHolder = (InventoryHolder) blockstate;
                inventory = inventoryHolder.getInventory();
            }
            for (ItemStack itemStack : inventory) {
                if (itemStack == null) {
                    continue;
                }
                Double worth = containerWorth.get(itemStack.getType().toString().toUpperCase());
                if (worth == null) {
                    worth = (double) 0;
                }
                totalContainerWorth += worth * itemStack.getAmount();
            }
        }
        return totalContainerWorth;
    }

    /**
     * Process the worth of spawners (for individuals which may need GUI).
     *
     * @param blocks list of spawner blocks
     *
     * @return double value representing total worth of spawners
     */
    public double processSpawnerWorthForIndividual(ArrayList<Block> blocks, UUID uuid,
            boolean useGui) {
        if (blocks == null) {
            return 0;
        }
        double totalSpawnerWorth = 0;
        for (Block block : blocks) {
            try {
                CreatureSpawner spawner = (CreatureSpawner) block.getState();
                EntityType mobType = spawner.getSpawnedType();
                String mobName = mobType.getName();
                if (mobName != null) {
                    String mobNameUpperCase = mobName.toUpperCase();
                    Double value = spawnerWorth.get(mobNameUpperCase);
                    if (value == null) {
                        value = 0.0;
                    } else if (useGui) {
                        landManager.setSenderSpawnerForGui(uuid, mobNameUpperCase);
                    }
                    totalSpawnerWorth += value;
                }
            } catch (ClassCastException e) {
                // error thrown if player breaks spawner just as calculation is taking place
            }
        }
        return totalSpawnerWorth;
    }

    /**
     * Process the worth of container items (for individuals who may need GUI).
     *
     * @param blocks list of containers
     *
     * @return double value representing total worth of container items
     */
    public double processContainerWorthForIndividual(ArrayList<Block> blocks, UUID uuid,
            boolean useGui) {
        if (blocks == null) {
            return 0;
        }
        double totalContainerWorth = 0;
        for (Block block : blocks) {
            BlockState blockstate = block.getState();
            Inventory inventory;
            if (blockstate instanceof Chest) {
                Chest chest = (Chest) blockstate;
                inventory = chest.getBlockInventory();
            } else {
                InventoryHolder inventoryHolder = (InventoryHolder) blockstate;
                inventory = inventoryHolder.getInventory();
            }
            for (ItemStack itemStack : inventory) {
                if (itemStack == null) {
                    continue;
                }
                String itemName = itemStack.getType().toString().toUpperCase();
                Double worth = containerWorth.get(itemName);
                if (worth == null) {
                    worth = (double) 0;
                } else if (useGui) {
                    landManager.setSenderContainerForGui(uuid, itemName, itemStack.getAmount());
                }
                totalContainerWorth += worth * itemStack.getAmount();
            }
        }
        return totalContainerWorth;
    }

    /**
     * Get the worth of a block.
     *
     * @param name name of block
     *
     * @return double representing its worth
     */
    public double getBlockWorth(String name) {
        Double value = this.materialWorth.get(name);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Get the worth of a spawner.
     *
     * @param name name of spawner
     *
     * @return double representing its worth
     */
    public double getSpawnerWorth(String name) {
        Double value = this.spawnerWorth.get(name);
        if (value == null) {
            return 0;
        }
        return value;
    }

    /**
     * Get the worth of a container item.
     *
     * @param name name of container item
     *
     * @return double representing its worth
     */
    public double getContainerWorth(String name) {
        Double value = this.containerWorth.get(name);
        if (value == null) {
            return 0;
        }
        return value;
    }

    private BiFunction<UUID, Block, Double> calculateBlockForAll = (uuid, block) -> {
        Double worth = materialWorth.get(block.getType().toString());
        if (worth == null) {
            return 0.0;
        }
        return worth;
    };

    private BiFunction<UUID, Block, Double> calculateSpawnerForAll = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            main.getLandManager().setSenderSpawnerForAll(uuid, block);
        }
        return 0.0;
    };



    private BiFunction<UUID, Block, Double> calculateContainerForAll = (uuid, block) -> {
        Material material = block.getType();
        if (containerTypes.contains(material)) {
            main.getLandManager().setSenderContainerForAll(uuid, block);
        }
        return 0.0;
    };

    private BiFunction<UUID, Block, Double> calculateBlockForIndividual = (uuid, block) -> {
        Double worth = materialWorth.get(block.getType().toString());
        if (worth == null) {
            return 0.0;
        }

        if (worth != 0) {
            main.getLandManager().setSenderBlockForGui(uuid, block);
        }
        return worth;
    };

    private BiFunction<UUID, Block, Double> calculateSpawnerForIndividual = (uuid, block) -> {
        Material material = block.getType();
        if (material.equals(Material.SPAWNER)) {
            main.getLandManager().setSenderSpawnerForIndividual(uuid, block);
        }
        return 0.0;
    };

    private BiFunction<UUID, Block, Double> calculateContainerForIndividual = (uuid, block) -> {
        Material material = block.getType();
        if (containerTypes.contains(material)) {
            main.getLandManager().setSenderContainerForIndividual(uuid, block);
        }
        return 0.0;
    };
}
