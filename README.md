# DbConector
1. Master/Slave ConnectionManager in Java :<br/>
– class called ConnectionManager with 2 database servers configured - PostgreSQL/MySQL<br/>
– using pool for database connections <br/>
– implements failover access to database <br/>
– if one database dies the ConnectionManager should automatically switch to second database server <br/>
– during failover mode checks if master DB server is again ready to use and establish all new connections to master DB server afterwards<br/>
<br/>
2. Logging System implement in Java: <br/>
– Log4j appender which stores log data into data storage <br/>
– fulltext search on log data <br/>
