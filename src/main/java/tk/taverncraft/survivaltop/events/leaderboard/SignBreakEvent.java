package tk.taverncraft.survivaltop.events.leaderboard;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.leaderboard.SignHelper;
import tk.taverncraft.survivaltop.utils.MessageManager;

/**
 * SignBreakEvent checks for when a SurvivalTop sign is placed.
 */
public class SignBreakEvent implements Listener {

    private final String signRemovePerm = "survtop.sign.remove";
    Main main;

    /**
     * Constructor for SignBreakEvent.
     */
    public SignBreakEvent(Main main) {
        this.main = main;
    }

    @EventHandler
    private void onSignBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        if (!block.getType().toString().contains("WALL_SIGN")) {
            return;
        }

        BlockState state = block.getState();
        if (!(state instanceof Sign)) {
            return;
        }

        Sign sign = (Sign) state;

        String line1 = sign.getLine(0);
        String line2 = sign.getLine(1);
        String unformattedLine2 = ChatColor.stripColor(line2);

        SignHelper signHelper = new SignHelper(main);
        if (!signHelper.isSurvTopSign(line1, line2)) {
            return;
        }

        Player player = e.getPlayer();

        if (!player.hasPermission(signRemovePerm)) {
            e.setCancelled(true);
            MessageManager.sendMessage(player, "no-survtop-sign-remove-permission");
            return;
        }

        signHelper.removeSign(block);
        MessageManager.sendMessage(player, "survtop-sign-broken",
                new String[]{"%rank%"},
                new String[]{unformattedLine2});
    }
}
