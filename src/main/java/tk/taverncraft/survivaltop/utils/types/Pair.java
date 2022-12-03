package tk.taverncraft.survivaltop.utils.types;

public class Pair {
    private final String message;
    private final net.md_5.bungee.api.ChatColor color;

    public Pair(String message, net.md_5.bungee.api.ChatColor color) {
        this.message = message;
        this.color = color;
    }

    public String getMessage() {
        return message;
    }

    public net.md_5.bungee.api.ChatColor getColor() {
        return color;
    }
}
