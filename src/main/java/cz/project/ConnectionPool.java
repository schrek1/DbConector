package cz.project;

import java.sql.SQLException;


public class ConnectionPool {

    private DbConnection[] pool;

    private String url, user, password;
    private boolean isMaster;
    private int poolSize;

    public static ConnectionPool createMasterPool(String url, String user, String password, int poolSize) {
        return new ConnectionPool(url, user, password, true, poolSize);
    }

    public static ConnectionPool createSlavePool(String url, String user, String password, int poolSize) {
        return new ConnectionPool(url, user, password, false, poolSize);
    }

    private ConnectionPool(String url, String user, String password, boolean isMaster, int poolSize) {
        pool = new DbConnection[poolSize];
        this.isMaster = isMaster;
        this.url = url;
        this.user = user;
        this.password = password;
        this.poolSize = poolSize;
    }

    public DbConnection getConnection() {
        for (int i = 0; i < pool.length; i++) {
            final DbConnection connection = pool[i];
            if (connection != null) {
                if (!connection.isUsing()) {
                    connection.lent();
                    return connection;
                }
            } else {
                DbConnection dbConnection = isMaster ? DbConnection.createMaster(url, user, password) : DbConnection.createSlave(url, user, password);
                dbConnection.lent();
                pool[i] = dbConnection;
                return dbConnection;
            }
        }
        return null;
    }

    public boolean hasFreeConnection() {
        for (int i = 0; i < pool.length; i++) {
            final DbConnection connection = pool[i];
            if (connection != null) {
                if (!connection.isUsing()) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    public void returnConnection(DbConnection returnedConnection) {
        if (returnedConnection != null) {
            for (int i = 0; i < pool.length; i++) {
                DbConnection dbConnection = pool[i];
                if (dbConnection == returnedConnection) {
                    dbConnection.restart();
                    return;
                }
            }
        }
    }

    public int getPoolSize() {
        return poolSize;
    }

    public void cleanPool() {
        for (int i = 0; i < pool.length; i++) {
            DbConnection connection = pool[i];
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    continue; // can't close connection, skip
                } finally {
                    pool[i] = null; // avoid memory leak
                }
            }
        }
    }
}
