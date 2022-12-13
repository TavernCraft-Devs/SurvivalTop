package tk.taverncraft.survivaltop.events.leaderboard;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.leaderboard.SignHelper;
import tk.taverncraft.survivaltop.messages.MessageManager;
import tk.taverncraft.survivaltop.permissions.PermissionsManager;

/**
 * SignPlaceEvent checks for when a leaderboard sign is placed.
 */
public class SignPlaceEvent implements Listener {
    private final Main main;
    private final PermissionsManager permissionsManager;

    /**
     * Constructor for SignPlaceEvent.
     *
     * @param main plugin class
     */
    public SignPlaceEvent(Main main) {
        this.main = main;
        this.permissionsManager = new PermissionsManager(main);
    }

    @EventHandler
    private void onSignPlace(SignChangeEvent e) {
        Block block = e.getBlock();

        if (!block.getType().toString().contains("WALL_SIGN")) {
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return;
        }

        String line1 = e.getLine(0);
        String line2 = e.getLine(1);
        SignHelper signHelper = new SignHelper(main);
        if (!signHelper.isSurvTopSign(line1, line2)) {
            return;
        }

        Player player = e.getPlayer();
        if (!permissionsManager.hasSignAddPerm(player)) {
            e.setCancelled(true);
            MessageManager.sendMessage(player, "no-survtop-sign-addTask-permission");
            return;
        }

        if (main.getLeaderboardManager().isUpdating()) {
            signHelper.updateSign(e.getBlock(), null, Integer.parseInt(line2),
                    "Updating...", "Updating...");
        } else {
            signHelper.updateSign(e.getBlock(), null, Integer.parseInt(line2),
                    "Not updated", "Not updated");
        }
        MessageManager.sendMessage(player, "survtop-sign-placed",
                new String[]{"%rank%"},
                new String[]{line2});
    }
}

