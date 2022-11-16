package tk.taverncraft.survivaltop.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import tk.taverncraft.survivaltop.Main;

/**
 * DependencyLoadEvent ensures plugin soft dependencies are loaded properly.
 */
public class DependencyLoadEvent implements Listener {
    private final Main main;

    // todo: check if this class is still necessary

    /**
     * Constructor for DependencyLoadEvent.
     *
     * @param main plugin class
     */
    public DependencyLoadEvent(Main main) {
        this.main = main;
    }

    // required to bypass a spigot bug with softdepend
    @EventHandler
    private void onServerLoad(ServerLoadEvent e) {
        main.loadDependencies();
    }
}

