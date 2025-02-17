# Database meta data API

The user may request
[meta data](https://javadoc.io/doc/io.github.guttenbase/guttenbase/latest/io/github/guttenbase/guttenbase/meta/package-summary.html)
from the connector repository that contains all the information about a data base.

## Code example
Read some data from the data base in a JUnit test.

```java
final DatabaseMetaData databaseMetaData = connectorRepository.getDatabase(CONNECTOR_ID);
assertNotNull(databaseMetaData);
assertEquals("Apache Derby", databaseMetaData.getDatabaseName());

assertEquals(6, databaseMetaData.getTable().size());
final TableMetaData userTableMetaData = databaseMetaData.getTable("FOO_USER");
assertNotNull(userTableMetaData);

assertEquals(6, userTableMetaData.getColumnCount());
final ColumnMetaData idColumn = userTableMetaData.getColumn("ID");
assertNotNull(idColumn);
assertEquals("BIGINT", idColumn.getColumnTypeName());
```

# Schema creation

GuttenBase supports the capability to build your own data base schema definitions.

## Code example

```java
...
final var tableMetaDataBuilder = new TableMetaDataBuilder(databaseMetaDataBuilder);
tableMetaDataBuilder.setTableName(tableMapper.mapTableName(sourceTableMetaData));
...
final var nameColumnBuilder = new ColumnMetaDataImpl(tableMetaDataBuilder, sourceColumnMetaData);   
tableMetaDataBuilder.addColumn(nameColumnBuilder);

final var indexBuilder =  new IndexMetaDataImpl(tableMetaDataBuilder, "NAME_IDX", true, true, false);
indexBuilder.addColumn(nameColumnBuilder)
    
tableMetaDataBuilder.addIndex(indexBuilder);
```
