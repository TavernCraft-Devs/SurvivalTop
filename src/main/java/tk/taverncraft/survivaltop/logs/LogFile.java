package tk.taverncraft.survivaltop.logs;

/**
 * A LogFile object for holding plugin specific information to include in logs.
 */
public class LogFile {
    private final String minecraftVersion;
    private final String survivalTopVersion;
    private final String worldSize;
    private final int numEntities;
    private final long numClaims;
    private final long numBlocks;
    private final long leaderboardUpdateStartTime;
    private final long lastUpdateDuration;
    private final long estimatedBlockProcessingRate;

    /**
     * Constructor for LogFile.
     *
     * @param minecraftVersion current version of minecraft
     * @param survivalTopVersion current version of survivaltop
     * @param worldSize size of the world
     * @param numEntities number of entities
     * @param numClaims number of claims
     * @param numBlocks number of blocks
     */
    public LogFile(String minecraftVersion, String survivalTopVersion, String worldSize,
            int numEntities, long numClaims, long numBlocks, long leaderboardUpdateStartTime,
            long lastUpdateDuration, long estimatedBlockProcessingRate) {
        this.minecraftVersion = minecraftVersion;
        this.survivalTopVersion = survivalTopVersion;
        this.worldSize = worldSize;
        this.numEntities = numEntities;
        this.numClaims = numClaims;
        this.numBlocks = numBlocks;
        this.leaderboardUpdateStartTime = leaderboardUpdateStartTime;
        this.lastUpdateDuration = lastUpdateDuration;
        this.estimatedBlockProcessingRate = estimatedBlockProcessingRate;
    }

    /**
     * Gets the current minecraft version.
     *
     * @return current version of minecraft
     */
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    /**
     * Gets the current survivaltop version.
     *
     * @return current version of survivaltop
     */
    public String getSurvivalTopVersion() {
        return this.survivalTopVersion;
    }

    /**
     * Gets the world size.
     *
     * @return world size
     */
    public String getWorldSize() {
        return this.worldSize;
    }

    /**
     * Gets the number of entities included in calculations.
     *
     * @return number of entities
     */
    public int getNumEntities() {
        return this.numEntities;
    }

    /**
     * Gets the number of claims included in calculations.
     *
     * @return number of claims
     */
    public long getNumClaims() {
        return this.numClaims;
    }

    /**
     * Gets the number of blocks included in calculations.
     *
     * @return number of blocks
     */
    public long getNumBlocks() {
        return this.numBlocks;
    }

    /**
     * Gets the start time of the last leaderboard update.
     *
     * @return start time of last leaderboard update
     */
    public long getLeaderboardUpdateStartTime() {
        return this.leaderboardUpdateStartTime;
    }

    /**
     * Gets the duration of the last leaderboard update.
     *
     * @return duration of last leaderboard update
     */
    public long getLastUpdateDuration() {
        return this.lastUpdateDuration;
    }

    /**
     * Gets the estimated block processing rate based on the last update (in seconds).
     *
     * @return estimated block processing rate
     */
    public long getEstimatedBlockProcessingRate() {
        return this.estimatedBlockProcessingRate;
    }
}
