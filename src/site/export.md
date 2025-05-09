# Export/Import SQL Dump

Using the _ExportSQLConnectorInfo_ class you may specify a file or output stream where to dump the SQL/DDL data. In combination
with the _CopySchemaTool_ you may export data to SQL data files using the target database specific syntax. I.e., you are
able to create a database dump for the target database offline!

*Warning:* You should do this only for small databases as the text file may become very large.
Optionally, the outputfile may be compressed with GZIP using the "compress" parameter in the connector info.

### Code example
```kotlin
val exportConnectorInfo = ExportSQLConnectorInfo(SOURCE, FILE, DatabaseType.MYSQL)
...
connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
connectorRepository.addConnectionInfo(SCRIPT, exportConnectorInfo)
...
val ddlScript = CopySchemaTool(connectorRepository).createDDLScript(SOURCE, SCRIPT)
DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, SCRIPT)
// SQL statements written to file during copying
val dataScript = File(FILE).readLines()
```

# Export/Import database meta

The _DatabaseMetaDataExporterTool_ allows you to export database meta data to a JSON file. This is useful for migrations where the
target database is not available online.

### Code example
```kotlin
val file = File.createTempFile(connectorId, ".json").apply { deleteOnExit() }

DatabaseMetaDataExporterTool(connectorRepository, connectorId).export(file)

val importedMetaData = importDataBaseMetaData(file, connectorId, connectorRepository)
```
