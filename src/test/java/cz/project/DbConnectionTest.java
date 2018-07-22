package cz.project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static cz.project.Const.*;
import static org.junit.jupiter.api.Assertions.*;

class DbConnectionTest {

    public static DbConnection dbConnection;


    @AfterEach
    void tearDown() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                System.err.println("can't close connection");
            }
        }
    }

    @Test
    void checkInit() throws SQLException {
        dbConnection = DbConnection.createMaster(MASTER_DB_URL, MASTER_DB_USER, MASTER_DB_PASS);

        assertEquals(dbConnection.isMaster(), true);
        assertEquals(dbConnection.isUsing(), false);
    }

    @Test
    void destroy_check() throws SQLException {
        dbConnection = DbConnection.createMaster(MASTER_DB_URL, MASTER_DB_USER, MASTER_DB_PASS);

        dbConnection.createDbConnection();

        assertNotNull(dbConnection.getConnection());
        dbConnection.close();
        assertNull(dbConnection.getConnection());
    }

    @Test
    void createDbConnection_toMaster() throws SQLException {
        dbConnection = DbConnection.createMaster(MASTER_DB_URL, MASTER_DB_USER, MASTER_DB_PASS);

        assertNotNull(dbConnection);

        dbConnection.createDbConnection();
        dbConnection.close();
    }

    @Test
    void createDbConnection_toSlave() throws SQLException {
        dbConnection = DbConnection.createSlave(SLAVE_DB_URL, SLAVE_DB_USER, SLAVE_DB_PASS);

        assertNotNull(dbConnection);

        dbConnection.createDbConnection();
        dbConnection.close();
    }

    @Test
    void createDbConnection_returnAgain() throws SQLException {
        dbConnection = DbConnection.createMaster(MASTER_DB_URL, MASTER_DB_USER, MASTER_DB_PASS);

        assertNotNull(dbConnection);

        Connection con1 = dbConnection.createDbConnection();
        Connection con2 = dbConnection.createDbConnection();

        assertTrue(con1 == con2);

        dbConnection.close();
    }


    @Test
    void createDbConnection_failConnect() throws SQLException {
        dbConnection = DbConnection.createMaster("dummy-url", "user", "pass");

        assertNotNull(dbConnection);

        assertThrows(SQLException.class, () -> dbConnection.createDbConnection());

        dbConnection.close();
    }

    @Test
    void refresh_changeState() throws SQLException {
        dbConnection = DbConnection.createMaster("dummy-url", "user", "pass");

        dbConnection.restart();
        assertFalse(dbConnection.isUsing());

        dbConnection.close();
    }

}