package io.github.guttenbase.connector

/**
 * Denote known/handled data bases. Easy to extend...
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
enum class DatabaseType {
  GENERIC,
  MOCK,
  EXPORT_DUMP,
  IMPORT_DUMP,
  MYSQL,
  MARIADB,
  POSTGRESQL,
  MSSQL,
  MS_ACCESS,
  HSQLDB,
  H2DB,
  DERBY,
  DB2,
  SYBASE,
  ORACLE,
  ORACLE_12
}
