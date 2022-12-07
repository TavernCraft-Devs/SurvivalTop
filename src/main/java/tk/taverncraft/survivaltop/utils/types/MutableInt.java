package tk.taverncraft.survivaltop.utils.types;

/**
 * Provides faster performance for tracking count of blocks.
 */
public class MutableInt {
    private int value = 0;

    /**
     * Increments value by 1.
     */
    public void increment() {
        ++value;
    }

    /**
     * Increments value by given amount.
     *
     * @param amount amount to increment by
     */
    public void increment(int amount) {
        value += amount;
    }

    /**
     * Gets the current value.
     *
     * @return current value
     */
    public int get() {
        return value;
    }
}