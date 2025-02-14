# Change log

## 4.1.1
- Remove unmaintained binary exporter/importer
- Export database related metadata to JSON
- Supply database metadata for offline usage when using ExportSQLConnector
- Remove MetaData suffix from methods and fields

## 4.0.1

- Complete refactoring of column type mapping algorithm: Use values supplied as supported types by the JDBC data base meta data.
- Use several passes in type resolution
- Breaking changes in API, simplifications and optimizations
- Many small code improvements and new tests
- Many bug fixes
- Internally tested schema and data migrations between many DB, such as DB2, MySQL, PostgreSQL, MSSQL, Oracle
- Introducing PreparedStatementPlaceholderFactory hint to allow for custom placeholder handling in prepared statements
- Removed MaxNumberOfDataItems hint and NumberOfRowsPerBatchHint and merged them into new BatchInsertionConfigurationHint hint
- Introduced ProprietaryColumnTypeDefinitionFactory to allow for custom column type handling
- New support for JDBC type ARRAY

## 3.4.0

- Use new progress bar indicator by default

## 3.3.4

- Fix encoding issues, i.e. allow for explicit encoding

## 3.3.3

- Generally map DB-specific types to their generic counterparts

## 3.3.2

- Filter synthetic indexes and foreign keys, i.e. constraints created by the database itself
- Fix plain text export issues

## 3.3.1

- Export data to SQL script via JDBC driver implementation
- Code cleanup and removal of deprecated code
- Blob handling for different types of RDBMS 

## 3.2.x

- Added support for builder pattern style in connector repository
- Added support to externalize and optionally encrypt database properties

## 3.1.x

- Added support for copying autoincrement columns across database systems (CopySchemaTool)
- Make connector repository reachable from anywhere in the meta model

## 3.0.x

- Reimplemention in Kotlin