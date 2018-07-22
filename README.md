# DbConector
1. Master/Slave ConnectionManager in Java : 
– class called ConnectionManager with 2 database servers configured - PostgreSQL/MySQL
– using pool for database connections 
– implements failover access to database 
– if one database dies the ConnectionManager should automatically switch to second database server 
– during failover mode checks if master DB server is again ready to use and establish all new connections to master DB server afterwards

2. Logging System implement in Java: 
– Log4j appender which stores log data into data storage 
– fulltext search on log data 
