package tk.taverncraft.survivaltop.logs;

public class LogFile {
    String minecraftVersion;
    String survivalTopVersion;
    String worldRadius;
    int numEntities;
    long numClaims;
    long numBlocks;

    public LogFile(String minecraftVersion, String survivalTopVersion, String worldRadius,
            int numEntities, long numClaims, long numBlocks) {
        this.minecraftVersion = minecraftVersion;
        this.survivalTopVersion = survivalTopVersion;
        this.worldRadius = worldRadius;
        this.numEntities = numEntities;
        this.numClaims = numClaims;
        this.numBlocks = numBlocks;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public String getSurvivalTopVersion() {
        return this.survivalTopVersion;
    }

    public String getWorldRadius() {
        return this.worldRadius;
    }

    public int getNumEntities() {
        return this.numEntities;
    }

    public long getNumClaims() {
        return this.numClaims;
    }

    public long getNumBlocks() {
        return this.numBlocks;
    }
}
