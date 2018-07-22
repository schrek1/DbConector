package cz.project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolTest {

    public static ConnectionPool connectionPool;

    @BeforeEach
    void setUp() {
        connectionPool = ConnectionPool.createMasterPool("dummy-url", "dummy-user", "dummy-pass", 4);
    }

    @AfterEach
    void tearDown() {
        if (connectionPool != null) {
            connectionPool.cleanPool();
        }
    }

    @Test
    void getConnections_connectionsInit() {
        for (int i = 0; i < connectionPool.getPoolSize(); i++) {
            DbConnection connection = connectionPool.getConnection();
            checkDbConnection(connection);

        }
        assertNull(connectionPool.getConnection());
    }

    @Test
    void getConnections_connectionsGetFree() {
        DbConnection connection = connectionPool.getConnection();
        checkDbConnection(connection);
        connection.setUsing(false);

        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void hasFreeConnection_notInit() {
        assertEquals(connectionPool.hasFreeConnection(), true);
    }

    @Test
    void hasFreeConnection_allUsed() {
        for (int i = 0; i < connectionPool.getPoolSize(); i++) {
            connectionPool.getConnection();
        }
        assertEquals(connectionPool.hasFreeConnection(), false);
    }

    @Test
    void hasFreeConnection_oneRemoved() {
        DbConnection dbConnection = null;
        for (int i = 0; i < connectionPool.getPoolSize(); i++) {
            dbConnection = connectionPool.getConnection();
        }

        if (dbConnection != null) {
            connectionPool.returnConnection(dbConnection);
        }

        assertEquals(connectionPool.hasFreeConnection(), true);
    }


    @Test
    void returnConnection_initAndRemove() {
        List<DbConnection> connections = new ArrayList<>();

        // fill
        for (int i = 0; i < connectionPool.getPoolSize(); i++) {
            assertEquals(connectionPool.hasFreeConnection(), true);
            DbConnection connection = connectionPool.getConnection();
            checkDbConnection(connection);
            connections.add(connection);
        }

        // full-house
        assertEquals(connectionPool.hasFreeConnection(), false);

        // return all
        for (DbConnection connection : connections) {
            connectionPool.returnConnection(connection);
            assertEquals(connectionPool.hasFreeConnection(), true);
        }

    }

    @Test
    void returnConnection_nullTest() {
        connectionPool.returnConnection(null);
    }

    private void checkDbConnection(DbConnection dbConnection) {
        assertNotNull(dbConnection);
        assertEquals(dbConnection.isUsing(), true);
    }
}