package cz.project;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Holder pro db connection
 */
public class DbConnection {
    private boolean isUsing;
    private boolean isMaster;
    private long lentTimestamp;
    private Connection connection;

    private String url, user, password;

    public static DbConnection createMaster(String url, String user, String password) {
        return new DbConnection(url, user, password, true);
    }

    public static DbConnection createSlave(String url, String user, String password) {
        return new DbConnection(url, user, password, false);
    }


    private DbConnection(String url, String user, String password, boolean isMaster) {
        this.isMaster = isMaster;
        this.url = url;
        this.user = user;
        this.password = password;

        isUsing = false;
    }

    public Connection createDbConnection() throws SQLException {
        try {
            if (connection != null && connection.isValid(1)) {
                // if ok... return exist connection
                return connection;
            } else {
                // nok... create new connection
                return connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException exception) {
            // not valid or not accessible
            throw exception;
        }

    }

    public void lent() {
        lentTimestamp = System.currentTimeMillis();
        isUsing = true;
    }

    public void restart() {
        lentTimestamp = -1;
        isUsing = false;
    }

    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public boolean isUsing() {
        return isUsing;
    }

    public void setUsing(boolean using) {
        isUsing = using;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean master) {
        isMaster = master;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public long getLentTimestamp() {
        return lentTimestamp;
    }

    public void setLentTimestamp(long lentTimestamp) {
        this.lentTimestamp = lentTimestamp;
    }
}
