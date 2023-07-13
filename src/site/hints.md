# Hints

The way tools operate is configured by hints. There is [quite a number of hints](https://javadoc.io/doc/de.akquinet.jbosscc.guttenbase/GuttenBase/latest/de/akquinet/jbosscc/guttenbase/hints/package-summary.html) you can use:

* ColumnDataMapperProviderHint: Used to find mappings for column data. E.g., when converting a number to a String or casting a LONG to a BIGINT.
* ColumnMapperHint: Select target column(s) for given source column. Usually, there will a 1:1 relationship. However, there may be situations where you want to duplicate or transform data into multiple columns.
* ColumnNameMapperHint: Map the way column names of a table are used. Usually you won't need that, but sometimes you want to map the names, e.g. to add name backticks, in order to escape special characters.
* ColumnOrderHint: Determine order of columns in SELECT statement. This will of course also influence the ordering of the resulting INSERT statement.
* ColumnTypeResolverListHint: Determine strategies to use for mapping different column types. It provides a list of column type resolvers which will be asked in turn to resolve a column type conflict.
* DatabaseTableFilterHint: Regard which tables when looking for tables in the given data base.
* EntityTableCheckerHint: Check if the given table is a "main" table in the sense that it represents an entity. In terms of JPA: the corresponding Java class is annotated with @Entity.
* ExporterFactoryHint: Configure Exporter to be used for dumping databases
* ExportDumpExtraInformationHint: When exporting to e JAR/ZIP file we give the user a possibility to add extra informations to the dumped data.
* ImporterFactoryHint: Configure Importer to be used for reading dumped databases
* ImportDumpExtraInformationHint: When exporting to JAR/ZIP file we give the user a possibility to retrieve extra informations from the dumped data.
* MaxNumberOfDataItemsHint: How many data items may an INSERT statement have. I.e., how many data items does the database support in satatement. This hint may in effect limit the number given by the NumberOfRowsPerInsertionHint
* NumberOfCheckedTableDataHint: How many rows of the copied tables shall be regarded when checking that data has been transferred correctly with the CheckEqualTableDataTool
* NumberOfRowsPerBatchHint: How many rows will be inserted in single transaction
* RepositoryColumnFilterHint: This filter is applied when requesting meta data from the connector repository
* RepositoryTableFilterHint:This filter is applied when requesting meta data from the connector repository
* SplitColumnHint: Define split column for SplitByRangeTableCopyTool
* TableMapperHint: Map tables between source and target data base
* TableNameMapperHint: Map table names, e.g. prepend schema name schema.table or add backticks (`) to escape special names.
* TableOrderHint: Determine order of tables during copying/comparison
* ZipExporterClassResourcesHint: When exporting to e JAR/ZIP file we allow to add custom classes and resources to the resulting JAR. This allows to create a self-contained executable JAR that will startup with a Main class specified by the framework user.

## Code example
Regard only tables starting with "tdm_":

```java
public class TdmTableFilterHint extends RepositoryTableFilterHint {
    public RepositoryTableFilter getValue() {
        return new RepositoryTableFilter() {
            public boolean accept(final TableMetaData table) {
              final String lowerCase = table.getTableName().toLowerCase();
              return lowerCase.startsWith("tdm_");
    }};
}}
...
connectorRepository.addConnectorHint(SOURCE, new TdmTableFilterHint());
```