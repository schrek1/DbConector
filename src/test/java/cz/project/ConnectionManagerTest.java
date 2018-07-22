package cz.project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.ConnectException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static cz.project.Const.POOL_SIZE;
import static org.junit.jupiter.api.Assertions.*;


class ConnectionManagerTest {

    private static ConnectionManager conMng;

    @BeforeEach
    void setUp() {
        conMng = new ConnectionManager(POOL_SIZE, POOL_SIZE);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void hasFreeConnections_fillAndRemove() throws ConnectException {
        assertTrue(conMng.hasFreeConnections());
        List<Connection> connections = getAllConnections();
        assertFalse(conMng.hasFreeConnections());
        for (Connection connection : connections) {
            conMng.returnConnection(connection);
        }
    }

    private List<Connection> getAllConnections() throws ConnectException {
        List<Connection> connections = new ArrayList<>();
        for (int i = 0; i < Const.POOL_SIZE; i++) {
            Connection connection = conMng.getConnection();
            if (connection != null) {
                connections.add(connection);
            }
        }
        return connections;
    }

}