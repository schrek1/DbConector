package cz.project;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static cz.project.Const.POOL_SIZE;

public class Main {

    public static Logger log = Logger.getLogger(Main.class);


    public static void main(String[] args) {
        log.info("init app...");
        ConnectionManager cMng = new ConnectionManager(POOL_SIZE, POOL_SIZE);
        List<Connection> connections = new ArrayList<>();

        log.debug("loop started...");
        while (true) {
            for (int i = 0; i < 4; i++) {
                log.debug("get " + i + " connection");
                if (cMng.hasFreeConnections()) {
                    try {
                        Connection connection = cMng.getConnection();
                        connections.add(connection);
                        log.debug("obtain " + i + " connection...");
                        log.trace("connection=" + connection.toString());
                    } catch (Exception ex) {
                        log.error("exception occurred...", ex);
                    }
                } else {
                    log.debug(i + " connection isn't free");
                }
            }


            log.debug("returning connections to pool...");
            Iterator<Connection> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Connection connection = iterator.next();
                cMng.returnConnection(connection);
                iterator.remove();
                log.trace("connection returned");
            }
            log.trace("all connections returned");

            Scanner s = new Scanner(System.in);
            System.out.println("Press enter to next round.....");
            s.nextLine();
            log.trace("loop round ended...");
        }
    }
}
