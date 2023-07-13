# Data base specific configuration

* Data base specific implementations
* The source DB is usually marked as read only
* Turn off auto-commit for the target DB and disable constraints temporarily so that the copying process can run really fast without checking temporary violations
* Users may add life cycle methods to add their own code

## Code example

```java
private int tablesCount;
...
connectorRepository.addSourceDatabaseConfiguration(DatabaseType.DERBY, new DefaultSourceDatabaseConfiguration(connectorRepository) {
    @Override
    public void afterTableCopy(final Connection connection, final String connectorId, final TableMetaData table) throws SQLException {
      tablesCount++;
    }
});
...
System.out.println(_tablesCount + " tables copied...");
```