package tk.taverncraft.survivaltop.leaderboard;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import org.bukkit.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.messages.MessageManager;

/**
 * SignHelper handles all the operations required to update/removeTask a leaderboard sign.
 */
public class SignHelper {
    private final Main main;

    /**
     * Constructor for SignHelper.
     *
     * @param main plugin class
     */
    public SignHelper(Main main) {
        this.main = main;
    }

    /**
     * Gets the key of a sign based on its block location.
     *
     * @param block block where sign is
     */
    public String getKey(Block block) {
        return block.getWorld().getName() + block.getX() + block.getY() + block.getZ();
    }

    /**
     * Checks if a sign is a SurvivalTop leaderboard sign.
     *
     * @param line1 first line of leaderboard sign
     * @param line2 second line of leaderboard sign
     */
    public boolean isSurvTopSign(String line1, String line2) {
        String unformattedLine1 = ChatColor.stripColor(line1);
        String unformattedLine2 = ChatColor.stripColor(line2);

        if (unformattedLine1 == null
                || !unformattedLine1.equalsIgnoreCase("[survivaltop]")) {
            return false;
        }

        try {
            Integer.parseInt(unformattedLine2);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Saves a newly created leaderboard sign into file.
     *
     * @param block block where sign is
     * @param position position of leaderboard
     * @param name name of entity at position
     * @param wealth wealth of entity at position
     */
    public void save(Block block, int position, String name, String wealth) {
        File signsFile = new File(main.getDataFolder(), "dat/signs.yml");
        if (!signsFile.exists()) {
            main.getConfigManager().createSignsConfig();
        }
        String key = getKey(block);
        FileConfiguration signsConfig = main.getConfigManager().getSignsConfig();
        signsConfig.set(key + ".x", block.getX());
        signsConfig.set(key + ".y", block.getY());
        signsConfig.set(key + ".z", block.getZ());
        signsConfig.set(key + ".world", block.getWorld().getName());
        signsConfig.set(key + ".direction",
                ((Directional) block.getBlockData()).getFacing().toString());
        signsConfig.set(key + ".rank", position);
        signsConfig.set(key + ".entity", name);
        signsConfig.set(key + ".wealth", wealth);

        try {
            signsConfig.save(signsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates all the signs after every leaderboard update.
     */
    public void updateSigns() throws NullPointerException {
        FileConfiguration config = main.getConfigManager().getSignsConfig();
        for (String key : Objects.requireNonNull(
                config.getConfigurationSection("")).getKeys(false)) {
            double x = config.getDouble(key + ".x");
            double y = config.getDouble(key + ".y");
            double z = config.getDouble(key + ".z");
            World world = Bukkit.getWorld(Objects.requireNonNull(
                config.getString(key + ".world")));
            int position = config.getInt(key + ".rank");
            String savedName = Objects.requireNonNull(
                config.getString(key + ".entity"));
            String savedWealth = Objects.requireNonNull(
                config.getString(key + ".wealth"));
            String direction = config.getString(key + ".direction");
            Location loc;
            try {
                loc = new Location(world, x, y, z);
            } catch (NullPointerException e) {
                return;
            }

            Block block = loc.getBlock();
            BlockState state = block.getState();
            if (!(state instanceof Sign)) {
                removeSign(block);
                continue;
            }

            updateSign(block, direction, position, savedName, savedWealth);
        }
    }

    /**
     * Updates a sign according to current leaderboard.
     *
     * @param block block where sign is
     * @param direction direction of sign
     * @param position position of leaderboard
     * @param savedName last saved entity name
     * @param savedWealth last saved entity wealth
     */
    public void updateSign(Block block, String direction, int position, String savedName,
            String savedWealth) {
        String name;
        String wealth;
        try {
            name = main.getCacheManager().getEntityNameAtPosition(position - 1);
            wealth = main.getCacheManager().getEntityWealthAtPosition(position - 1);
        } catch (IndexOutOfBoundsException ex) {
            name = savedName;
            wealth = savedWealth;
        }

        Sign sign = (Sign) block.getState();
        if (direction != null) {
            Directional dir = (Directional) sign.getBlockData();
            dir.setFacing(BlockFace.valueOf(direction));
        }
        String signFormat = MessageManager.getSignFormat(new String[]{"%entity%", "%totalwealth%"},
                new String[]{name, wealth});
        String[] signArr = signFormat.split("\n", 2);

        sign.setLine(0, ChatColor.translateAlternateColorCodes('&',
                "&b&l[SurvivalTop]"));
        sign.setLine(1, ChatColor.translateAlternateColorCodes('&',
                "&e&l" + position));
        sign.setLine(2, signArr[0]);
        sign.setLine(3, signArr[1]);
        Bukkit.getScheduler().runTask(main, (Runnable) sign::update);
        save(block, position, name, wealth);

        PlayerHeadHelper playerHeadHelper = new PlayerHeadHelper();
        Block skullAboveBlockBehindSign = playerHeadHelper.getSkullAboveBlockBehindSign(block);
        Block skullAboveSign = playerHeadHelper.getSkullAboveSign(block);
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        if (main.getOptions().groupIsEnabled()) {
            String leader = main.getGroupManager().getGroupLeader(name);
            if (leader == null) {
                player = Bukkit.getOfflinePlayer("MHF_QUESTION");
            } else {
                player = Bukkit.getOfflinePlayer(leader);
            }
        }
        playerHeadHelper.update(player, skullAboveBlockBehindSign, skullAboveSign);
    }

    /**
     * Removes a SurvivalTop leaderboard sign.
     *
     * @param block block where sign is
     */
    public void removeSign(Block block) {
        String key = getKey(block);
        File signsFile = new File(main.getDataFolder(), "dat/signs.yml");
        if (!signsFile.exists()) {
            main.getConfigManager().createSignsConfig();
        }
        FileConfiguration signsConfig = main.getConfigManager().getSignsConfig();
        signsConfig.set(key, null);
        try {
            signsConfig.save(signsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
