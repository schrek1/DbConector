package cz.project;

public class Const {
    public static final String ELASTIC_LOG_INDEX_NAME = "logs";
    public static final String MYSQL_TIME_ZONE_PARAM = "?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    public static final String MASTER_DB_URL = "jdbc:mysql://localhost/test" + MYSQL_TIME_ZONE_PARAM;
    public static final String MASTER_DB_USER = "root";
    public static final String MASTER_DB_PASS = "";

    public static final String SLAVE_DB_URL = "jdbc:postgresql://localhost/postgres";
    public static final String SLAVE_DB_USER = "postgres";
    public static final String SLAVE_DB_PASS = "";

    public static final int POOL_SIZE = 2;

}
