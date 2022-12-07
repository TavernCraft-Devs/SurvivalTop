package tk.taverncraft.survivaltop.utils.types;

/**
 * Utility class used to help parse colours in text components.
 */
public class TextComponentPair {
    private final String message;
    private final net.md_5.bungee.api.ChatColor colour;

    /**
     * Constructor for TextComponentPair.
     *
     * @param message message to construct pair with as key
     * @param colour colour to construct pair with as value
     */
    public TextComponentPair(String message, net.md_5.bungee.api.ChatColor colour) {
        this.message = message;
        this.colour = colour;
    }

    /**
     * Gets the message from the pair.
     *
     * @return message from pair
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the colour from the pair.
     *
     * @return colour from pair
     */
    public net.md_5.bungee.api.ChatColor getColor() {
        return colour;
    }
}
