package io.github.guttenbase.mapping

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.DatabaseType.*
import io.github.guttenbase.meta.ColumnMetaData
import java.sql.Types
import java.util.*

/**
 * Default uses same data type as source
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 */
@Suppress("MemberVisibilityCanBePrivate")
open class DefaultColumnTypeMapper : ColumnTypeMapper {
  private val mappings = HashMap<DatabaseType, MutableMap<DatabaseType, MutableMap<String, ColumnDefinition>>>()

  init {
    createPostgresToMysqlMapping()
    createOracleToPostgresMapping()
    createMysqlToPostresMapping()
    createMysqlToOracle()
    createOracleToMysql()
    createMssqlToOracle()
    createMssqlToMysql()
    createMssqlToPostgres()
    createH2ToDerbyMapping()
    createDerbyToH2Mapping()
    createDB2ToMysqlMapping()
    createDB2ToPostgresMapping()
    createMysqltoDB2Mapping()
    createPostgrestoDB2Mapping()
    createMysqlToMssqlMapping()
    createPostgresToMssqlMapping()
    createDB2ToMssqlMapping()
    createPostgresToOracleMapping()
  }

  /**
   * @return target database type including precision and optional not null, autoincrement, and primary key constraint clauses
   */
  override fun mapColumnType(
    column: ColumnMetaData, sourceDatabaseType: DatabaseType, targetDatabaseType: DatabaseType
  ): String {
    val columnDefinition = lookupColumnDefinition(sourceDatabaseType, targetDatabaseType, column)
      ?: createDefaultColumnDefinition(column, "")
    val precision = createPrecisionClause(column, columnDefinition.precision)
    val singlePrimaryKey = column.isPrimaryKey && column.tableMetaData.primaryKeyColumns.size < 2
    val autoincrementClause =
      if (column.isAutoIncrement) " " + lookupAutoIncrementClause(column, targetDatabaseType) else ""
    val notNullClause = if (column.isNullable || singlePrimaryKey) "" else " NOT NULL" // Primary key implies NOT NULL
    val primaryKeyClause = if (singlePrimaryKey) " PRIMARY KEY" else ""

    return columnDefinition.type + precision + notNullClause + autoincrementClause + primaryKeyClause
  }

  /**
   * Override this method if you just want to change the way column types are logically mapped
   *
   * @return target database type including precision
   */
  @Suppress("SameParameterValue")
  protected fun createDefaultColumnDefinition(
    columnMetaData: ColumnMetaData,
    optionalPrecision: String
  ): ColumnDefinition {
    val precision = createPrecisionClause(columnMetaData, optionalPrecision)
    val defaultColumnType: String = columnMetaData.columnTypeName

    return ColumnDefinition(defaultColumnType, precision)
  }

  protected fun createPrecisionClause(columnMetaData: ColumnMetaData, optionalPrecision: String): String {
    return when (columnMetaData.columnType) {
      Types.CHAR, Types.VARCHAR, Types.VARBINARY -> if (columnMetaData.columnTypeName.uppercase().contains("TEXT")) {
        "" // TEXT does not support precision
      } else {
        val precision = columnMetaData.precision

        if (precision > 0) "($precision)" else ""
      }

      else -> optionalPrecision
    }
  }

  protected fun lookupColumnDefinition(
    sourceDatabaseType: DatabaseType,
    targetDatabaseType: DatabaseType,
    column: ColumnMetaData
  ): ColumnDefinition? {
    if (column.isAutoIncrement) {
      val columnType = targetDatabaseType.createColumnType(column)

      if (columnType != null) {
        return ColumnDefinition(columnType, "")
      }
    }

    val columnType = column.columnTypeName.uppercase()
    val databaseMatrix = mappings[sourceDatabaseType]

    if (databaseMatrix != null) {
      val databaseMapping = databaseMatrix[targetDatabaseType]

      if (databaseMapping != null) {
        return databaseMapping[columnType]
      }
    }

    return null
  }

  /**
   * ID columns may be defined as autoincremented, i.e. every time data is inserted the ID will be incremented autoimatically.
   * Unfortunately every database has its own way to implement this feature.
   *
   * @return the autoincrement clause for the target database
   */
  protected fun lookupAutoIncrementClause(column: ColumnMetaData, targetDatabaseType: DatabaseType) =
    targetDatabaseType.createColumnAutoincrementClause(column)

  @JvmOverloads
  fun addMapping(
    sourceDB: DatabaseType,
    targetDB: DatabaseType,
    sourceTypeName: String,
    targetTypeName: String,
    precision: String = ""
  ): DefaultColumnTypeMapper {
    addMappingInternal(sourceDB, targetDB, sourceTypeName, targetTypeName, precision)

    if (sourceDB === MYSQL) {
      addMappingInternal(MARIADB, targetDB, sourceTypeName, targetTypeName, precision)
    } else if (targetDB === MYSQL) {
      addMappingInternal(sourceDB, MARIADB, sourceTypeName, targetTypeName, precision)
    }

    return this
  }

  private fun addMappingInternal(
    sourceDB: DatabaseType,
    targetDB: DatabaseType,
    sourceTypeName: String,
    targetTypeName: String,
    precision: String
  ) {
    val databaseMatrix = mappings.getOrPut(sourceDB) { EnumMap(DatabaseType::class.java) }
    val mapping = databaseMatrix.getOrPut(targetDB) { HashMap() }

    mapping[sourceTypeName] = ColumnDefinition(targetTypeName, precision)
  }

  private fun createPostgresToMysqlMapping() {
    addMapping(POSTGRESQL, MYSQL, "INT8", "TINYINT")
    addMapping(POSTGRESQL, MYSQL, "ARRAY", "LONGTEXT")
    addMapping(POSTGRESQL, MYSQL, "BIGSERIAL", "BIGINT")
    addMapping(POSTGRESQL, MYSQL, "BOOLEAN", "TINYINT(1)")
    addMapping(POSTGRESQL, MYSQL, "BOX", "POLYGON")
    addMapping(POSTGRESQL, MYSQL, "BYTEA", "LONGBLOB")
    addMapping(POSTGRESQL, MYSQL, "CIDR", "VARCHAR(43)")
    addMapping(POSTGRESQL, MYSQL, "CIRCLE", "POLYGON")
    addMapping(POSTGRESQL, MYSQL, "DOUBLE PRECISION", "DOUBLE")
    addMapping(POSTGRESQL, MYSQL, "INET", "VARCHAR", "(43)")
    addMapping(POSTGRESQL, MYSQL, "INTERVAL", "TIME")
    addMapping(POSTGRESQL, MYSQL, "JSON", "LONGTEXT")
    addMapping(POSTGRESQL, MYSQL, "LINE", "LINESTRING")
    addMapping(POSTGRESQL, MYSQL, "LSEG", "LINESTRING")
    addMapping(POSTGRESQL, MYSQL, "MACADDR", "VARCHAR", "(17)")
    addMapping(POSTGRESQL, MYSQL, "MONEY", "DECIMAL", "(19,2)")
    addMapping(POSTGRESQL, MYSQL, "NATIONAL CHARACTER VARYING", "VARCHAR")
    addMapping(POSTGRESQL, MYSQL, "NATIONAL CHARACTER", "CHAR")
    addMapping(POSTGRESQL, MYSQL, "NUMERIC", "DECIMAL")
    addMapping(POSTGRESQL, MYSQL, "PATH", "LINESTRING")
    addMapping(POSTGRESQL, MYSQL, "REAL", "FLOAT")
    addMapping(POSTGRESQL, MYSQL, "SERIAL", "INT")
    addMapping(POSTGRESQL, MYSQL, "SMALLSERIAL", "SMALLINT")
    addMapping(POSTGRESQL, MYSQL, "TEXT", "LONGTEXT")
    addMapping(POSTGRESQL, MYSQL, "TIMESTAMP", "DATETIME")
    addMapping(POSTGRESQL, MYSQL, "TSQUERY", "LONGTEXT")
    addMapping(POSTGRESQL, MYSQL, "TSVECTOR", "LONGTEXT")
    addMapping(POSTGRESQL, MYSQL, "TXID_SNAPSHOT", "VARCHAR")
    addMapping(POSTGRESQL, MYSQL, "UUID", "VARCHAR", "(36)")
    addMapping(POSTGRESQL, MYSQL, "XML", "LONGTEXT")
    addMapping(POSTGRESQL, MYSQL, "OID", "BLOB")
  }

  private fun createOracleToPostgresMapping() {
    addMapping(ORACLE, POSTGRESQL, "NUMBER", "NUMERIC")
    addMapping(ORACLE, POSTGRESQL, "VARCHAR2", "VARCHAR")
    addMapping(ORACLE, POSTGRESQL, "RAW", "BYTEA")
  }

  private fun createPostgresToOracleMapping() {
    addMapping(POSTGRESQL, ORACLE, "BPCHAR", "CHAR", "(1)")
    addMapping(POSTGRESQL, ORACLE, "BPCHAR", "CHAR")
    addMapping(POSTGRESQL, ORACLE, "INT8", "NUMBER", "(3)")
    addMapping(POSTGRESQL, ORACLE, "INT4", "NUMBER", "(5)")
    addMapping(POSTGRESQL, ORACLE, "INT16", "NUMBER", "(10)")
    addMapping(POSTGRESQL, ORACLE, "BIGSERIAL", "NUMBER", "(19)")
    addMapping(POSTGRESQL, ORACLE, "OID", "BLOB")
  }

  private fun createH2ToDerbyMapping() {
    addMapping(H2DB, DERBY, "LONGTEXT", "CLOB")
    addMapping(H2DB, DERBY, "LONGBLOB", "BLOB")
  }

  private fun createDerbyToH2Mapping() {
    addMapping(DERBY, H2DB, "LONGTEXT", "CLOB")
    addMapping(DERBY, H2DB, "LONGBLOB", "BLOB")
  }

  private fun createMysqltoDB2Mapping() {
    addMapping(MYSQL, DB2, "LONGTEXT", "VARCHAR", "(4000)") //CHAR(254)
    addMapping(MYSQL, DB2, "LONGBLOB", "BLOB") //CLOB (2G)
    addMapping(MYSQL, DB2, "DECIMAL", "DECIMAL", "(16)") //CLOB (2G)
  }

  private fun createPostgrestoDB2Mapping() {
    addMapping(POSTGRESQL, DB2, "TEXT", "VARCHAR", "(4000)") //CHAR(254)
    addMapping(POSTGRESQL, DB2, "BYTEA", "BLOB") //CLOB (2G) LONGBLOB
    addMapping(POSTGRESQL, DB2, "NUMERIC", "DECIMAL", "(16)")
    addMapping(POSTGRESQL, DB2, "INT(2)", "DECIMAL", "(16)")
    addMapping(POSTGRESQL, DB2, "INT(4)", "DECIMAL", "(16)")
  }

  private fun createDB2ToMysqlMapping() {
    addMapping(DB2, MYSQL, "CHAR", "CHAR")
    addMapping(DB2, MYSQL, "CLOB", "LONGBLOB")
    addMapping(DB2, MYSQL, "INTEGER", "INT", "(11)")
  }

  private fun createDB2ToPostgresMapping() {
    addMapping(DB2, POSTGRESQL, "BLOB", "BYTEA")
  }

  private fun createDB2ToMssqlMapping() {
    addMapping(DB2, MSSQL, "BLOB", "VARBINARY")
  }

  private fun createMysqlToPostresMapping() {
    addMapping(MYSQL, POSTGRESQL, "BIGINT AUTO_INCREMENT", "BIGSERIAL")
    addMapping(MYSQL, POSTGRESQL, "BIGINT UNSIGNED", "NUMERIC", "(20)")
    addMapping(MYSQL, POSTGRESQL, "INTEGER UNSIGNED", "BIGINT")
    addMapping(MYSQL, POSTGRESQL, "INT UNSIGNED", "BIGINT")
    addMapping(MYSQL, POSTGRESQL, "MEDIUMINT UNSIGNED", "INTEGER")
    addMapping(MYSQL, POSTGRESQL, "BINARY", "BYTEA")
    addMapping(MYSQL, POSTGRESQL, "BLOB", "BYTEA")
    addMapping(MYSQL, POSTGRESQL, "DATETIME", "TIMESTAMP")
    addMapping(MYSQL, POSTGRESQL, "DOUBLE", "DOUBLE PRECISION")
    addMapping(MYSQL, POSTGRESQL, "FLOAT", "REAL")
    addMapping(MYSQL, POSTGRESQL, "INTEGER AUTO_INCREMENT", "SERIAL")
    addMapping(MYSQL, POSTGRESQL, "LONGBLOB", "BYTEA")
    addMapping(MYSQL, POSTGRESQL, "MEDIUMINT", "INTEGER")
    addMapping(MYSQL, POSTGRESQL, "MEDIUMBLOB", "BYTEA")
    addMapping(MYSQL, POSTGRESQL, "TINYBLOB", "BYTEA")
    addMapping(MYSQL, POSTGRESQL, "LONGTEXT", "TEXT")
    addMapping(MYSQL, POSTGRESQL, "MEDIUMTEXT", "TEXT")
    addMapping(MYSQL, POSTGRESQL, "SMALLINT AUTO_INCREMENT", "SMALLSERIAL")
    addMapping(MYSQL, POSTGRESQL, "SMALLINT UNSIGNED", "INTEGER")
    addMapping(MYSQL, POSTGRESQL, "TINYINT", "NUMERIC(1)")
    addMapping(MYSQL, POSTGRESQL, "TINYINT AUTO_INCREMENT", "SMALLSERIAL")
    addMapping(MYSQL, POSTGRESQL, "TINYINT UNSIGNED", "SMALLSERIAL")
    addMapping(MYSQL, POSTGRESQL, "TINYTEXT", "TEXT")
    addMapping(MYSQL, POSTGRESQL, "VARBINARY", "BYTEA")
  }

  private fun createMysqlToOracle() {
    addMapping(MYSQL, ORACLE, "BIT", "RAW")
    addMapping(MYSQL, ORACLE, "BIGINT", "NUMBER", "(19, 0)")
    addMapping(MYSQL, ORACLE, "DATETIME", "DATE")
    addMapping(MYSQL, ORACLE, "DECIMAL", "FLOAT", "(24)")
    addMapping(MYSQL, ORACLE, "DOUBLE", "FLOAT", "(24)")
    addMapping(MYSQL, ORACLE, "DOUBLE PRECISION", "FLOAT", "(24)")
    addMapping(MYSQL, ORACLE, "ENUM", "VARCHAR2")
    addMapping(MYSQL, ORACLE, "INT", "NUMBER", "(10, 0)")
    addMapping(MYSQL, ORACLE, "INTEGER", "NUMBER", "(10, 0)")
    addMapping(MYSQL, ORACLE, "LONGBLOB", "BLOB")
    addMapping(MYSQL, ORACLE, "LONGTEXT", "CLOB")
    addMapping(MYSQL, ORACLE, "MEDIUMBLOB", "BLOB")
    addMapping(MYSQL, ORACLE, "MEDIUMINT", "NUMBER", "(7, 0)")
    addMapping(MYSQL, ORACLE, "MEDIUMTEXT", "CLOB")
    addMapping(MYSQL, ORACLE, "NUMERIC", "NUMBER")
    addMapping(MYSQL, ORACLE, "REAL", "FLOAT", "(24)")
    addMapping(MYSQL, ORACLE, "SET", "VARCHAR2")
    addMapping(MYSQL, ORACLE, "SMALLINT", "NUMBER", "(5, 0)")
    addMapping(MYSQL, ORACLE, "TEXT", "VARCHAR2")
    addMapping(MYSQL, ORACLE, "TIME", "DATE")
    addMapping(MYSQL, ORACLE, "TIMESTAMP", "DATE")
    addMapping(MYSQL, ORACLE, "TINYBLOB", "RAW")
    addMapping(MYSQL, ORACLE, "TINYINT", "NUMBER", "(3, 0)")
    addMapping(MYSQL, ORACLE, "TINYTEXT", "VARCHAR2")
    addMapping(MYSQL, ORACLE, "YEAR", "NUMBER")
    addMapping(MYSQL, ORACLE, "VARBINARY", "BYTEA")
  }

  private fun createOracleToMysql() {
    addMapping(ORACLE, MYSQL, "RAW", "BIT") //TINYBLOB
    addMapping(ORACLE, MYSQL, "NUMBER(19, 0)", "BIGINT")
    addMapping(ORACLE, MYSQL, "DATE", "DATETIME")
    addMapping(ORACLE, MYSQL, "FLOAT (24)", "DECIMAL") // DOUBLE, DOUBLE PRECISION, REAL
    addMapping(ORACLE, MYSQL, "VARCHAR2", "VARCHAR")
    addMapping(ORACLE, MYSQL, "NUMBER(10, 0)", "INT") // INTEGER
    addMapping(ORACLE, MYSQL, "BLOB", "LONGBLOB") // MEDIUMBLOB
    addMapping(ORACLE, MYSQL, "CLOB", "MEDIUMTEXT")
    addMapping(ORACLE, MYSQL, "NUMBER(7, 0)", "MEDIUMINT")
    addMapping(ORACLE, MYSQL, "NUMBER", "NUMERIC") //YEAR
    addMapping(ORACLE, MYSQL, "NUMBER(5, 0)", "SMALLINT")
    addMapping(ORACLE, MYSQL, "BYTEA", "VARBINARY")
  }

  private fun createMssqlToOracle() {
    addMapping(MSSQL, ORACLE, "BIGINT", "NUMBER", "(19)")
    addMapping(MSSQL, ORACLE, "BINARY", "RAW")
    addMapping(MSSQL, ORACLE, "BIT", "NUMBER", "(1)")
    addMapping(MSSQL, ORACLE, "DATETIME", "DATE")
    addMapping(MSSQL, ORACLE, "DECIMAL", "NUMBER", "(10)")
    addMapping(MSSQL, ORACLE, "FLOAT", "FLOAT", "(49)")
    addMapping(MSSQL, ORACLE, "IMAGE", "LONG RAW")
    addMapping(MSSQL, ORACLE, "INTEGER", "NUMBER", "(10")
    addMapping(MSSQL, ORACLE, "MONEY", "NUMBER", "(19,4)")
    addMapping(MSSQL, ORACLE, "NTEXT", "LONG")
    addMapping(MSSQL, ORACLE, "NVARCHAR", "NCHAR")
    addMapping(MSSQL, ORACLE, "NUMERIC", "NUMBER", "(10)")
    addMapping(MSSQL, ORACLE, "REAL", "FLOAT", "(23)")
    addMapping(MSSQL, ORACLE, "SMALL DATETIME", "DATE")
    addMapping(MSSQL, ORACLE, "SMALL MONEY", "NUMBER", "(10,4)")
    addMapping(MSSQL, ORACLE, "SMALLINT", "NUMBER", "(5)")
    addMapping(MSSQL, ORACLE, "TEXT", "LONG")
    addMapping(MSSQL, ORACLE, "TIMESTAMP", "RAW")
    addMapping(MSSQL, ORACLE, "TINYINT", "NUMBER", "(3)")
    addMapping(MSSQL, ORACLE, "UNIQUEIDENTIFIER", "CHAR", "(36)")
    addMapping(MSSQL, ORACLE, "VARBINARY", "RAW")
    addMapping(MSSQL, ORACLE, "VARCHAR", "VARCHAR2")
  }

  private fun createMysqlToMssqlMapping() {
    addMapping(MYSQL, MSSQL, "LONGTEXT", "NVARCHAR", "(4000)")
    // MSSQL is very weird concering timestamps
    // java.sql.BatchUpdateException: Cannot insert an explicit value into a timestamp column. Use INSERT with a column list to exclude the timestamp column, or insert a DEFAULT into the timestamp column.
    addMapping(MYSQL, MSSQL, "TIMESTAMP", "DATETIME")
    addMapping(MYSQL, MSSQL, "LONGBLOB", "VARBINARY")
    addMapping(MYSQL, MSSQL, "DECIMAL", "DECIMAL", "(38)")
  }

  private fun createPostgresToMssqlMapping() {
    addMapping(POSTGRESQL, MSSQL, "TEXT", "NVARCHAR", "(4000)")
    addMapping(POSTGRESQL, MSSQL, "BYTEA", "BINARY")
    addMapping(POSTGRESQL, MSSQL, "INT4", "INT")
    addMapping(POSTGRESQL, MSSQL, "INT2", "INT")
  }

  private fun createMssqlToMysql() {
    addMapping(MSSQL, MYSQL, "NVARCHAR", "VARCHAR", "(255)")
    addMapping(MSSQL, MYSQL, "VARBINARY", "BLOB")
  }

  private fun createMssqlToPostgres() {
    addMapping(MSSQL, POSTGRESQL, "NVARCHAR", "TEXT")
    addMapping(MSSQL, POSTGRESQL, "VARBINARY", "BYTEA")
  }

  data class ColumnDefinition(val type: String, val precision: String)
}
