package cz.project;

import java.net.ConnectException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {

    private static final int CONNECTION_CHECKER_TIMEOUT = 500;

    private ConnectionPool masterPool;
    private ConnectionPool slavePool;

    private Map<Connection, DbConnection> lentConnection;

    private volatile boolean isUsedMaster;
    private volatile boolean checkerRunning;


    public ConnectionManager(int masterPoolSize, int slavePoolSize) {
        lentConnection = new HashMap<>();
        masterPool = ConnectionPool.createMasterPool(Const.MASTER_DB_URL, Const.MASTER_DB_USER, Const.MASTER_DB_PASS, masterPoolSize);
        slavePool = ConnectionPool.createSlavePool(Const.SLAVE_DB_URL, Const.SLAVE_DB_USER, Const.SLAVE_DB_PASS, slavePoolSize);

        checkerRunning = false;
        isUsedMaster = true;
    }

    public boolean hasFreeConnections() {
        if (isUsedMaster) {
            return masterPool.hasFreeConnection();
        } else {
            return slavePool.hasFreeConnection();
        }
    }

    public Connection getConnection() throws ConnectException {
        if (isUsedMaster) {
            return getMasterConnection();
        } else {
            return getSlaveConnection();
        }
    }

    public void returnConnection(Connection returnConnection) {
        if (returnConnection != null) {
            DbConnection dbConnection = lentConnection.get(returnConnection);
            if (dbConnection != null) {
                if (dbConnection.isMaster()) {
                    masterPool.returnConnection(dbConnection);
                } else {
                    slavePool.returnConnection(dbConnection);
                }
                lentConnection.remove(returnConnection);
            }
        }
    }

    private Connection getMasterConnection() throws ConnectException {
        if (masterPool.hasFreeConnection()) {
            DbConnection dbConnection = null;
            try {
                dbConnection = masterPool.getConnection();
                Connection connection = dbConnection.createDbConnection();
                lentConnection.put(connection, dbConnection);

                return connection;
            } catch (SQLException exception) {
                // connection fail -> try connect slave
                masterPool.returnConnection(dbConnection); // db-connection not used -> return back to pool
                isUsedMaster = false;
                checkMasterConnection();
                return getSlaveConnection();
            }
        } else {
            // pool is empty
            return null;
        }
    }

    private synchronized Connection getSlaveConnection() throws ConnectException {
        if (slavePool.hasFreeConnection()) {
            DbConnection dbConnection = null;
            try {
                dbConnection = slavePool.getConnection();
                Connection connection = dbConnection.createDbConnection();
                lentConnection.put(connection, dbConnection);

                return connection;
            } catch (SQLException e) {
                // connection fail -> switch to master again
                slavePool.returnConnection(dbConnection); // db-connection not used -> return back to pool
                isUsedMaster = true;
                throw new ConnectException("can't access to slave db");
            }
        } else {
            // pool is empty
            return null;
        }
    }

    private void checkMasterConnection() {
        if (!checkerRunning) {
            new Thread(() -> {
                while (true) {
                    try {
                        checkerRunning = true;

                        DriverManager.getConnection(Const.MASTER_DB_URL, Const.MASTER_DB_USER, Const.MASTER_DB_PASS); // try reconnect

                        // if success connection
                        isUsedMaster = true; // switch to master db
                        checkerRunning = false; // unlock check-tread to use

                        return;
                    } catch (SQLException ex) {
                        try {
                            Thread.sleep(CONNECTION_CHECKER_TIMEOUT); // timeout
                        } catch (InterruptedException e) {
                            checkerRunning = false; // open check-tread to use
                            return;
                        }
                    }
                }
            }).start();
        }
    }


}
