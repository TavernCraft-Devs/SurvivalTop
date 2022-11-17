package tk.taverncraft.survivaltop.utils.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Consumer;

/**
 * Checks if there is an update plugin and prompts users to update.
 */
public class PluginUpdateManager {

    private final JavaPlugin plugin;
    private final int resourceId;

    /**
     * Constructor for PluginUpdateManager.
     *
     * @param plugin plugin class
     *
     * @param resourceId id of the resource obtained from spigot link
     */
    public PluginUpdateManager(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    /**
     * Get latest version from spigot.
     *
     * @param consumer function deciding message to send after version is obtained
     */
    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL(
                    "https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId)
                        .openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                this.plugin.getLogger().info("Cannot look for updates: "
                        + exception.getMessage());
            }
        });
    }
}
