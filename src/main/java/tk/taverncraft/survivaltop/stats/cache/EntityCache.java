package tk.taverncraft.survivaltop.stats.cache;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;

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

    /**
     * Constructor for EntityCache.
     *
     * @param balWealth balance wealth of entity
     * @param blockWealth block wealth of entity
     * @param spawnerWealth spawner wealth of entity
     * @param containerWealth container wealth of entity
     * @param inventoryWealth inventory wealth of entity
     */
    public EntityCache(String name, double balWealth, HashMap<String, Double> papiWealth,
            double blockWealth, double spawnerWealth, double containerWealth, double inventoryWealth) {
        this.name = name;
        wealthBreakdown = new HashMap<>();
        wealthBreakdown.put("balance-wealth", balWealth);
        wealthBreakdown.putAll(papiWealth);
        wealthBreakdown.put("block-wealth", blockWealth);
        wealthBreakdown.put("spawner-wealth", spawnerWealth);
        wealthBreakdown.put("container-wealth", containerWealth);
        wealthBreakdown.put("inventory-wealth", inventoryWealth);
        double landWealth = blockWealth + spawnerWealth + containerWealth;
        wealthBreakdown.put("land-wealth", landWealth);
        double totalWealth = papiWealth.values().stream().mapToDouble(Double::valueOf).sum()
            + balWealth + landWealth + inventoryWealth;
        wealthBreakdown.put("total-wealth", totalWealth);
        this.cacheTime = Instant.now().getEpochSecond();
    }

    /**
     * Gets the gui of the entity (if applicable).
     *
     * @return gui of entity
     */
    public StatsGui getGui(Main main) {
        if (gui == null) {
            setGui(main);
        }
        return gui;
    }

    public void setGui(Main main) {
        this.gui = main.getGuiManager().getStatsGui(name, wealthBreakdown,
                blockCounter, spawnerCounter, containerCounter, inventoryCounter);
    }

    public void createChat() {
        if (this.placeholders != null && this.values != null) {
            return;
        }
        String strTotalWealth = String.format("%.02f", getTotalWealth());
        String strBalWealth = String.format("%.02f", getBalWealth());
        String strLandWealth = String.format("%.02f", getLandWealth());
        String strBlockWealth = String.format("%.02f", getBlockWealth());
        String strSpawnerWealth = String.format("%.02f", getSpawnerWealth());
        String strContainerWealth = String.format("%.02f", getContainerWealth());
        String strInvWealth = String.format("%.02f", getInventoryWealth());

        this.placeholders = new String[]{"%entity%", "%landwealth%", "%balwealth%",
            "%totalwealth%", "%blockwealth%", "%spawnerwealth%", "%containerwealth%",
            "%inventorywealth%"};

        this.values = new String[]{name, new BigDecimal(strLandWealth).toPlainString(),
            new BigDecimal(strBalWealth).toPlainString(),
            new BigDecimal(strTotalWealth).toPlainString(),
            new BigDecimal(strBlockWealth).toPlainString(),
            new BigDecimal(strSpawnerWealth).toPlainString(),
            new BigDecimal(strContainerWealth).toPlainString(),
            new BigDecimal(strInvWealth).toPlainString()};
    }

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

    public void setCounters(HashMap<String, MutableInt> blockCounter, HashMap<String, MutableInt> spawnerCounter,
        HashMap<String, MutableInt> containerCounter, HashMap<String, MutableInt> inventoryCounter) {
        this.blockCounter = blockCounter;
        this.spawnerCounter = spawnerCounter;
        this.containerCounter = containerCounter;
        this.inventoryCounter = inventoryCounter;
    }

    public String[] getPlaceholders() {
        return placeholders;
    }

    public String[] getValues() {
        return values;
    }

    public boolean isExpired(long cacheDuration) {
        return Instant.now().getEpochSecond() - cacheTime >= cacheDuration;
    }
}
