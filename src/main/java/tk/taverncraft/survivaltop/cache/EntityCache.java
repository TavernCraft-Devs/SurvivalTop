package tk.taverncraft.survivaltop.cache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Stream;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.gui.types.StatsGui;
import tk.taverncraft.survivaltop.utils.types.MutableInt;

/**
 * Stores cache data for entity if realtime stats is disabled.
 */
public class EntityCache {

    // used for gui stats
    private StatsGui gui;

    // used for chat stats
    private String[] placeholders;
    private String[] values;

    // used to track expiry time
    private final long cacheTime;

    // entity information
    private final String name;
    private HashMap<String, MutableInt> blockCounter;
    private HashMap<String, MutableInt> spawnerCounter;
    private HashMap<String, MutableInt> containerCounter;
    private HashMap<String, MutableInt> inventoryCounter;

    // contains breakdown of entity wealth
    private final HashMap<String, Double> wealthBreakdown;
    private final LinkedHashMap<String, Double> papiWealth;

    /**
     * Constructor for EntityCache.
     *
     * @param balWealth balance wealth of entity
     * @param papiWealth papi wealth of entity
     * @param blockWealth block wealth of entity
     * @param spawnerWealth spawner wealth of entity
     * @param containerWealth container wealth of entity
     * @param inventoryWealth inventory wealth of entity
     */
    public EntityCache(String name, double balWealth, LinkedHashMap<String, Double> papiWealth,
            double blockWealth, double spawnerWealth, double containerWealth,
            double inventoryWealth) {
        this.name = name;
        this.papiWealth = papiWealth;
        wealthBreakdown = new HashMap<>();
        wealthBreakdown.put("balance-wealth", new BigDecimal(balWealth).setScale(2,
                RoundingMode.HALF_UP).doubleValue());
        wealthBreakdown.putAll(papiWealth);
        wealthBreakdown.put("block-wealth", new BigDecimal(blockWealth).setScale(2,
                RoundingMode.HALF_UP).doubleValue());
        wealthBreakdown.put("spawner-wealth", new BigDecimal(spawnerWealth).setScale(2,
                RoundingMode.HALF_UP).doubleValue());
        wealthBreakdown.put("container-wealth", new BigDecimal(containerWealth).setScale(2,
                RoundingMode.HALF_UP).doubleValue());
        wealthBreakdown.put("inventory-wealth", new BigDecimal(inventoryWealth).setScale(2,
                RoundingMode.HALF_UP).doubleValue());
        double landWealth = blockWealth + spawnerWealth + containerWealth;
        wealthBreakdown.put("land-wealth", new BigDecimal(landWealth)
                .setScale(2, RoundingMode.HALF_UP).doubleValue());
        double totalWealth = papiWealth.values().stream().mapToDouble(Double::valueOf).sum() +
                balWealth + landWealth + inventoryWealth;
        wealthBreakdown.put("total-wealth", new BigDecimal(totalWealth)
                .setScale(2, RoundingMode.HALF_UP).doubleValue());
        this.cacheTime = Instant.now().getEpochSecond();
    }

    /**
     * Gets the gui of the entity (if applicable).
     *
     * @param main plugin class
     *
     * @return gui of entity
     */
    public StatsGui getGui(Main main) {
        if (gui == null) {
            setGui(main);
        }
        return gui;
    }

    /**
     * Sets the gui stats for the entity.
     *
     * @param main plugin class
     */
    public void setGui(Main main) {
        this.gui = main.getGuiManager().getStatsGui(name, wealthBreakdown,
                blockCounter, spawnerCounter, containerCounter, inventoryCounter);
    }

    /**
     * Sets the chat stats for the entity.
     */
    public void setChat() {
        if (this.placeholders != null && this.values != null) {
            return;
        }

        Stream<String> placeholdersStream = Stream.concat(Stream.of("entity"),
                wealthBreakdown.keySet().stream());
        this.placeholders = placeholdersStream.map(e -> "%" + e + "%").toArray(String[]::new);

        Stream<String> valuesStream = Stream.concat(Stream.of(name),
            wealthBreakdown.values().stream().map(e ->
                new BigDecimal(e).setScale(2, RoundingMode.HALF_UP).toPlainString()));
        this.values = valuesStream.toArray(String[]::new);
    }

    /**
     * Gets the name of the entity.
     *
     * @return name of entity
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the balance of wealth of the entity.
     *
     * @return balance wealth of entity
     */
    public double getBalWealth() {
        return wealthBreakdown.get("balance-wealth");
    }

    /**
     * Gets the block wealth of the entity
     *
     * @return block wealth of entity
     */
    public double getBlockWealth() {
        return wealthBreakdown.get("block-wealth");
    }

    /**
     * Gets spawner wealth of the entity
     *
     * @return spawner wealth of the entity
     */
    public double getSpawnerWealth() {
        return wealthBreakdown.get("spawner-wealth");
    }

    /**
     * Gets the container wealth of the entity
     *
     * @return container wealth of the entity
     */
    public double getContainerWealth() {
        return wealthBreakdown.get("container-wealth");
    }

    /**
     * Gets the inventory wealth of the entity
     *
     * @return inventory wealth of the entity
     */
    public double getInventoryWealth() {
        return wealthBreakdown.get("inventory-wealth");
    }

    /**
     * Gets the land wealth of the entity, calculated by summing up block wealth,
     * spawner wealth and container wealth.
     *
     * @return land wealth of the entity
     */
    public double getLandWealth() {
        return wealthBreakdown.get("land-wealth");
    }

    /**
     * Gets the wealth breakdown for the entity.
     *
     * @return wealth breakdown of the entity
     */
    public HashMap<String, Double> getWealthBreakdown() {
        return wealthBreakdown;
    }

    /**
     * Gets the wealth values from papi.
     *
     * @return papi wealth of the entity
     */
    public LinkedHashMap<String, Double> getPapiWealth() {
        return papiWealth;
    }

    /**
     * Gets the total wealth of the entity, calculated by summing up bal wealth, land wealth
     * and inv wealth.
     *
     * @return total wealth of the entity
     */
    public Double getTotalWealth() {
        return wealthBreakdown.get("total-wealth");
    }

    /**
     * Gets the time since epoch when the cache was created.
     *
     * @return time when cache was created
     */
    public long getCacheTime() {
        return cacheTime;
    }

    /**
     * Sets the counters of the entity.
     *
     * @param blockCounter counter for blocks
     * @param spawnerCounter counter for spawners
     * @param containerCounter counter for containers
     * @param inventoryCounter counter for inventories
     */
    public void setCounters(HashMap<String, MutableInt> blockCounter,
            HashMap<String, MutableInt> spawnerCounter,
            HashMap<String, MutableInt> containerCounter,
            HashMap<String, MutableInt> inventoryCounter) {
        this.blockCounter = blockCounter;
        this.spawnerCounter = spawnerCounter;
        this.containerCounter = containerCounter;
        this.inventoryCounter = inventoryCounter;
    }

    /**
     * Gets the placeholder keys used in entity cache chat stats.
     *
     * @return placeholder keys in entity cache chat stats
     */
    public String[] getPlaceholders() {
        return placeholders;
    }

    /**
     * Gets the placeholder values used in entity cache chat stats.
     *
     * @return placeholder values in entity cache chat stats
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Checks if cache is expired.
     *
     * @param cacheDuration duration before expiry
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired(long cacheDuration) {
        return Instant.now().getEpochSecond() - cacheTime >= cacheDuration;
    }
}
