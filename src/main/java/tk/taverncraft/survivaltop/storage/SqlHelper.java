package tk.taverncraft.survivaltop.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import tk.taverncraft.survivaltop.Main;

/**
 * SqlHelper is responsible for reading/writing from MySQL database.
 */
public class SqlHelper implements StorageHelper {
    private Main main;
    private String tableName;

    // todo: improve this terrible implementation
    public static String query = "";

    /**
     * Constructor for SqlHelper.
     *
     * @param main plugin class
     */
    public SqlHelper(Main main) {
        this.main = main;
    }

    /**
     * Updates the stats of an entity into a sql query for a single insertion into database.
     *
     * @param uuid uuid of entity to update stats for
     * @param landWealth the amount of wealth calculated from land
     * @param balWealth the amount of wealth calculated from balance
     */
    public void saveToStorage(UUID uuid, double landWealth, double balWealth) {
        String entityName = null;
        String entityType = "player";
        if (this.main.groupIsEnabled()) {
            entityName = this.main.getServerStatsManager().getGroupUuidToNameMap().get(uuid);
            entityType = "group";
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            if (player != null) {
                entityName = player.getName();
            }
        }
        SqlHelper.query += "('" + uuid + "', '" + entityName + "', '" + entityType + "', '"
                + landWealth + "', '" + balWealth + "'), ";
    }

    /**
     * Connects to MySQL database.
     *
     * @return returns a valid connection on success or null otherwise
     */
    public Connection connectToSql() {
        try {
            Connection conn;
            String dbName = main.getConfig().getString("database-name", "survtop");
            String tableName = main.getConfig().getString("table-name", "survtop");
            String port = main.getConfig().getString("port", "3306");
            String url = "jdbc:mysql://" + main.getConfig().getString("host") + ":"
                    + port + "/" + dbName + "?useSSL=false";
            String user = main.getConfig().getString("user", "survtop");
            String password = main.getConfig().getString("password");

            conn = DriverManager.getConnection(url, user, password);

            if (!databaseExists(dbName, conn)) {
                return null;
            }

            if (!tableExists(tableName, conn)) {
                String query = "CREATE TABLE " + tableName + "("
                        + "UUID VARCHAR (36) NOT NULL, "
                        + "ENTITY_NAME VARCHAR (36) NOT NULL, "
                        + "ENTITY_TYPE VARCHAR (10) NOT NULL, "
                        + "LAND_WEALTH DECIMAL (18, 2), "
                        + "BALANCE_WEALTH DECIMAL (18, 2), "
                        + "PRIMARY KEY (UUID))";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.executeUpdate();
                stmt.close();
            }
            return conn;

        } catch (SQLException e){
            Bukkit.getLogger().warning(e.getMessage());
            return null;
        }
    }

    /**
     * Checks if database exist.
     *
     * @param dbName name of database
     * @param conn an open connection
     *
     * @return true if database exist, false otherwise
     */
    public boolean databaseExists(String dbName, Connection conn) throws SQLException {
        ResultSet rs;
        if (conn != null) {

            rs = conn.getMetaData().getCatalogs();

            while (rs.next()) {
                String catalogs = rs.getString(1);

                if (dbName.equals(catalogs)) {
                    main.getLogger().info("The database " + dbName + " has been found.");
                    return true;
                }
            }

        } else {
            Bukkit.getLogger().info("Unable to connect to database.");
        }
        return false;
    }

    /**
     * Checks if table exist and create if not.
     *
     * @param tableName name of table
     * @param conn an open connection
     *
     * @return true if table exist, false otherwise
     */
    public boolean tableExists(String tableName, Connection conn) throws SQLException {
        boolean found = false;
        DatabaseMetaData databaseMetaData = conn.getMetaData();
        ResultSet rs = databaseMetaData.getTables(null, null,
                tableName, null);
        while (rs.next()) {
            String name = rs.getString("TABLE_NAME");
            if (tableName.equals(name)) {
                found = true;
                break;
            }
        }
        this.tableName = tableName;

        return found;
    }

    /**
     * Inserts user values into database.
     */
    public void insertIntoDatabase() {
        if (query.length() == 0) {
            return;
        }

        String header = "INSERT INTO " + tableName + "(UUID, ENTITY_NAME, ENTITY_TYPE, " +
            "LAND_WEALTH, BALANCE_WEALTH) VALUES ";
        String footer = " ON DUPLICATE KEY UPDATE LAND_WEALTH = VALUES(LAND_WEALTH), " +
            "BALANCE_WEALTH = VALUES(BALANCE_WEALTH)";
        String finalQuery = header + query.substring(0, query.length() - 2) + footer;
        try (Connection conn = this.connectToSql(); PreparedStatement stmt =
                conn.prepareStatement(finalQuery)) {
            if (conn != null) {
                stmt.executeUpdate();
            }
        } catch (NullPointerException | SQLException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
        query = "";
    }
}
