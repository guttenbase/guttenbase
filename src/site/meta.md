# Database meta data API

The user may request
[meta data](https://javadoc.io/doc/io.github.guttenbase/guttenbase/latest/io/github/guttenbase/guttenbase/meta/package-summary.html)
from the connector repository that contains all the information about a data base.

## Code example
Read some data from the data base in a JUnit test.

```java
final DatabaseMetaData databaseMetaData = connectorRepository.getDatabaseMetaData(CONNECTOR_ID);
assertNotNull(databaseMetaData);
assertEquals("Apache Derby", databaseMetaData.getDatabaseName());

assertEquals(6, databaseMetaData.getTableMetaData().size());
final TableMetaData userTableMetaData = databaseMetaData.getTableMetaData("FOO_USER");
assertNotNull(userTableMetaData);

assertEquals(6, userTableMetaData.getColumnCount());
final ColumnMetaData idColumn = userTableMetaData.getColumnMetaData("ID");
assertNotNull(idColumn);
assertEquals("BIGINT", idColumn.getColumnTypeName());
```

# Builder pattern

GuttenBase supports the builder design pattern to build your own data base schema definitions.

## Code example

```java
final TableMetaDataBuilder tableMetaDataBuilder = new TableMetaDataBuilder(_databaseMetaDataBuilder).setTableName(tableMapper
    .mapTableName(sourceTableMetaData));
...
tableMetaDataBuilder.addColumn(nameColumnBuilder).addIndex(new IndexMetaDataBuilder(tableMetaDataBuilder).
setAscending(true).setIndexName("NAME_IDX").
setUnique(true).addColumn(nameColumnBuilder));
```
