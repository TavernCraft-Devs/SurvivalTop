package tk.taverncraft.survivaltop.utils.types;

/**
 * Provides faster performance for tracking count of blocks.
 */
public class MutableInt {
    private int value = 0;

    public void increment() {
        ++value;
    }

    public void increment(int amount) {
        value += amount;
    }

    public int get() {
        return value;
    }
}