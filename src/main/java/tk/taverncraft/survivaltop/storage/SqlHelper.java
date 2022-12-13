package tk.taverncraft.survivaltop.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import tk.taverncraft.survivaltop.Main;
import tk.taverncraft.survivaltop.logs.LogManager;
import tk.taverncraft.survivaltop.papi.PapiManager;
import tk.taverncraft.survivaltop.cache.EntityCache;

/**
 * SqlHelper is responsible for reading/writing from MySQL database.
 */
public class SqlHelper implements StorageHelper {
    private final Main main;
    private String dbName;
    private String tableName;
    private String port;
    private String url;
    private String user;
    private String password;

    /**
     * Constructor for SqlHelper.
     *
     * @param main plugin class
     */
    public SqlHelper(Main main) {
        this.main = main;
        initializeConnectionInfo();
    }

    /**
     * Initialize default values for connection.
     */
    private void initializeConnectionInfo() {
        dbName = main.getOptions().getDatabaseName();
        tableName = main.getOptions().getTableName();
        port = main.getOptions().getPort();
        url = "jdbc:mysql://" + main.getOptions().getHost() + ":"
            + port + "/" + dbName + "?useSSL=false";
        user = main.getOptions().getUser();
        password = main.getOptions().getPassword();
    }

    /**
     * Saves information to mysql database.
     *
     * @param EntityCacheList list of entities to store
     */
    public void saveToStorage(ArrayList<EntityCache> EntityCacheList) {
        PapiManager papiManager = main.getPapiManager();
        List<String> categories = new ArrayList<>();
        if (papiManager != null) {
            categories = papiManager.getPapiCategories();
        }
        StringBuilder columns = new StringBuilder("ENTITY_NAME, ENTITY_TYPE, TOTAL_WEALTH, " +
            "LAND_WEALTH, BALANCE_WEALTH, BLOCK_WEALTH, SPAWNER_WEALTH, CONTAINER_WEALTH, " +
            "INVENTORY_WEALTH, ");
        for (String category : categories) {
            columns.append(category.toUpperCase().replaceAll("-", "_")).append(", ");
        }
        columns = new StringBuilder(columns.substring(0, columns.length() - 2));
        String header = "INSERT INTO " + tableName + " (" + columns + ") VALUES ";
        StringBuilder body = new StringBuilder();
        int cacheSize = EntityCacheList.size();
        for (int i = 0; i < cacheSize; i++) {
            EntityCache eCache = EntityCacheList.get(i);
            body.append(getEntityQuery(eCache));
        }

        if (body.length() == 0) {
            return;
        }
        String finalQuery = header + body.substring(0, body.length() - 2) + ";";
        try (Connection conn = this.connectToSql(); PreparedStatement stmt =
                conn.prepareStatement(finalQuery)) {
            if (conn != null) {
                stmt.executeUpdate();
            }
        } catch (NullPointerException | SQLException e) {
            LogManager.error(e.getMessage());
        }
    }

    /**
     * Connects to MySQL database.
     *
     * @return returns a valid connection on success or null otherwise
     */
    public Connection connectToSql() {
        try {
            Connection conn;
            conn = DriverManager.getConnection(url, user, password);

            if (!databaseExists(dbName, conn)) {
                return null;
            }

            PapiManager papiManager = main.getPapiManager();
            List<String> categories = new ArrayList<>();
            if (papiManager != null) {
                categories = papiManager.getPapiCategories();
            }
            StringBuilder columns = new StringBuilder();
            for (String category : categories) {
                columns.append(category.toUpperCase().replaceAll("-", "_"))
                        .append(" DECIMAL (18, 2), ");
            }
            if (tableExists(tableName, conn)) {
                PreparedStatement delStmt = conn.prepareStatement("DROP TABLE " + tableName);
                delStmt.execute();
                delStmt.close();
            }
            String query = "CREATE TABLE " + tableName + "("
                    + "ENTITY_NAME VARCHAR (36) NOT NULL, "
                    + "ENTITY_TYPE VARCHAR (10) NOT NULL, "
                    + "TOTAL_WEALTH DECIMAL (18, 2), "
                    + "LAND_WEALTH DECIMAL (18, 2), "
                    + "BALANCE_WEALTH DECIMAL (18, 2), "
                    + "BLOCK_WEALTH DECIMAL (18, 2), "
                    + "SPAWNER_WEALTH DECIMAL (18, 2), "
                    + "CONTAINER_WEALTH DECIMAL (18, 2), "
                    + "INVENTORY_WEALTH DECIMAL (18, 2), "
                    + columns
                    + "PRIMARY KEY (ENTITY_NAME))";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.executeUpdate();
            stmt.close();
            return conn;

        } catch (SQLException e){
            LogManager.warn(e.getMessage());
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
                    return true;
                }
            }

        } else {
            LogManager.error("Unable to connect to database.");
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
        return found;
    }

    /**
     * Appends individual entities to sql query.
     *
     * @param eCache entity to append
     */
    public String getEntityQuery(EntityCache eCache) {
        String entityName = eCache.getName();
        String entityType = "player";
        if (this.main.getOptions().groupIsEnabled()) {
            entityType = "group";
        }
        LinkedHashMap<String, Double> papiWealth = eCache.getPapiWealth();
        StringBuilder papiValues = new StringBuilder("', '");
        for (Double value : papiWealth.values()) {
            papiValues.append(value).append("', '");
        }
        papiValues = new StringBuilder(papiValues.substring(0, papiValues.length() - 4) + "'), ");
        return "('" + entityName + "', '" + entityType + "', '"
            + eCache.getTotalWealth() + "', '" + eCache.getLandWealth() + "', '"
            + eCache.getBalWealth() + "', '" + eCache.getBlockWealth() + "', '"
            + eCache.getSpawnerWealth() + "', '" + eCache.getContainerWealth() + "', '"
            + eCache.getInventoryWealth() + papiValues;
    }
}
