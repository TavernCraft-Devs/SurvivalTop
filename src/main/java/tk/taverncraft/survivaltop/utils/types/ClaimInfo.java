package tk.taverncraft.survivaltop.utils.types;

/**
 * Utility class for consolidating claim info.
 */
public class ClaimInfo {
    private final long numClaims;
    private final long numBlocks;

    /**
     * Constructor for ClaimInfo.
     *
     * @param numClaims number of claims
     * @param numBlocks number of blocks
     */
    public ClaimInfo(long numClaims, long numBlocks) {
        this.numClaims = numClaims;
        this.numBlocks = numBlocks;
    }

    /**
     * Gets the number of claims.
     *
     * @return number of claims
     */
    public long getNumClaims() {
        return numClaims;
    }

    /**
     * Gets the number of blocks.
     *
     * @return number of blocks
     */
    public long getNumBlocks() {
        return numBlocks;
    }
}
