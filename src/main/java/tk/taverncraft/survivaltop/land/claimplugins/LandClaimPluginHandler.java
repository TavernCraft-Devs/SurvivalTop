package tk.taverncraft.survivaltop.land.claimplugins;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiFunction;

import org.bukkit.block.Block;

public interface LandClaimPluginHandler {
    double getLandWorth(UUID uuid, String name, ArrayList<BiFunction<UUID, Block, Double>> blockOperations);
}
