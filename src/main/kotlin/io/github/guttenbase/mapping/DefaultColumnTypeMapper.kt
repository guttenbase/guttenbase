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
 */
@Suppress("MemberVisibilityCanBePrivate")
open class DefaultColumnTypeMapper : ColumnTypeMapper {
  private val mappings = HashMap<DatabaseType, MutableMap<DatabaseType, MutableMap<String, ColumnDefinition>>>()

  init {
    // DB data types to Standard SQL, if possible
    createH2SpecificMappings()
    createDerbySpecificMappings()
    createPostgresSpecificMappings()
    createOracleSpecificMappings()
    createMssqlSpecificMappings()
    createMysqlSpecificMappings()

    // Mappings between specific DBs, may override above configurations
    createMysqlToPostgresMapping()
    createMysqlToDB2Mapping()
    createMysqlToMssqlMapping()
    createMysqlToOracle()

    createOracleToPostgresMapping()

    createPostgresToMssqlMapping()

    createMssqlToOracle()
    createMssqlToPostgres()

    createDB2ToMysqlMapping()
    createDB2ToPostgresMapping()
    createDB2ToMssqlMapping()
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
    val defaultValueClause = lookupDefaultValueClause(columnDefinition, targetDatabaseType)

    return columnDefinition.type + precision + defaultValueClause + notNullClause + autoincrementClause + primaryKeyClause
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

  protected fun lookupDefaultValueClause(columnDefinition: ColumnDefinition, targetDatabaseType: DatabaseType) =
    when (targetDatabaseType) {
      MYSQL -> if (columnDefinition.type.equals("TIMESTAMP", true)) {
        " DEFAULT CURRENT_TIMESTAMP"    // Otherwise may result in [42000][1067] Invalid default value for 'CREATED_AT'
      } else {
        ""
      }

      else -> ""
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

  private fun createPostgresSpecificMappings() {
    mapDBspecificTypeToStandard(POSTGRESQL, "TEXT", "VARCHAR", "(4000)") //CHAR(254)
    mapDBspecificTypeToStandard(POSTGRESQL, "BYTEA", "BLOB") //CLOB (2G) LONGBLOB
    mapDBspecificTypeToStandard(POSTGRESQL, "NUMERIC", "DECIMAL", "(16)")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT(2)", "DECIMAL", "(16)")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT(4)", "DECIMAL", "(16)")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT4", "INT")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT2", "INT")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT8", "TINYINT")
    mapDBspecificTypeToStandard(POSTGRESQL, "ARRAY", "LONGTEXT")
    mapDBspecificTypeToStandard(POSTGRESQL, "BIGSERIAL", "BIGINT")
    mapDBspecificTypeToStandard(POSTGRESQL, "BOOLEAN", "TINYINT(1)")
    mapDBspecificTypeToStandard(POSTGRESQL, "BOX", "POLYGON")
    mapDBspecificTypeToStandard(POSTGRESQL, "BYTEA", "LONGBLOB")
    mapDBspecificTypeToStandard(POSTGRESQL, "CIDR", "VARCHAR(43)")
    mapDBspecificTypeToStandard(POSTGRESQL, "CIRCLE", "POLYGON")
    mapDBspecificTypeToStandard(POSTGRESQL, "DOUBLE PRECISION", "DOUBLE")
    mapDBspecificTypeToStandard(POSTGRESQL, "INET", "VARCHAR", "(43)")
    mapDBspecificTypeToStandard(POSTGRESQL, "INTERVAL", "TIME")
    mapDBspecificTypeToStandard(POSTGRESQL, "JSON", "LONGTEXT")
    mapDBspecificTypeToStandard(POSTGRESQL, "LINE", "LINESTRING")
    mapDBspecificTypeToStandard(POSTGRESQL, "LSEG", "LINESTRING")
    mapDBspecificTypeToStandard(POSTGRESQL, "MACADDR", "VARCHAR", "(17)")
    mapDBspecificTypeToStandard(POSTGRESQL, "MONEY", "DECIMAL", "(19,2)")
    mapDBspecificTypeToStandard(POSTGRESQL, "NATIONAL CHARACTER VARYING", "VARCHAR")
    mapDBspecificTypeToStandard(POSTGRESQL, "NATIONAL CHARACTER", "CHAR")
    mapDBspecificTypeToStandard(POSTGRESQL, "NUMERIC", "DECIMAL")
    mapDBspecificTypeToStandard(POSTGRESQL, "PATH", "LINESTRING")
    mapDBspecificTypeToStandard(POSTGRESQL, "REAL", "FLOAT")
    mapDBspecificTypeToStandard(POSTGRESQL, "SERIAL", "INT")
    mapDBspecificTypeToStandard(POSTGRESQL, "SMALLSERIAL", "SMALLINT")
    mapDBspecificTypeToStandard(POSTGRESQL, "TEXT", "LONGTEXT")
    mapDBspecificTypeToStandard(POSTGRESQL, "TIMESTAMP", "DATETIME")
    mapDBspecificTypeToStandard(POSTGRESQL, "TSQUERY", "LONGTEXT")
    mapDBspecificTypeToStandard(POSTGRESQL, "TSVECTOR", "LONGTEXT")
    mapDBspecificTypeToStandard(POSTGRESQL, "TXID_SNAPSHOT", "VARCHAR")
    mapDBspecificTypeToStandard(POSTGRESQL, "UUID", "VARCHAR", "(36)")
    mapDBspecificTypeToStandard(POSTGRESQL, "XML", "LONGTEXT")
    mapDBspecificTypeToStandard(POSTGRESQL, "BPCHAR", "CHAR", "(1)")
    mapDBspecificTypeToStandard(POSTGRESQL, "BPCHAR", "CHAR")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT8", "NUMBER", "(3)")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT4", "NUMBER", "(5)")
    mapDBspecificTypeToStandard(POSTGRESQL, "INT16", "NUMBER", "(10)")
    mapDBspecificTypeToStandard(POSTGRESQL, "BIGSERIAL", "NUMBER", "(19)")
    mapDBspecificTypeToStandard(POSTGRESQL, "OID", "BLOB")
    mapDBspecificTypeToStandard(POSTGRESQL, "BYTEA", "BINARY")
  }

  private fun createOracleSpecificMappings() {
    mapDBspecificTypeToStandard(ORACLE, "VARCHAR2", "VARCHAR")
    mapDBspecificTypeToStandard(ORACLE, "RAW", "BYTEA")
    mapDBspecificTypeToStandard(ORACLE, "RAW", "BIT") //TINYBLOB
    mapDBspecificTypeToStandard(ORACLE, "NUMBER(19, 0)", "BIGINT")
    mapDBspecificTypeToStandard(ORACLE, "DATE", "DATETIME")
    mapDBspecificTypeToStandard(ORACLE, "FLOAT(24)", "DECIMAL") // DOUBLE, DOUBLE PRECISION, REAL
    mapDBspecificTypeToStandard(ORACLE, "NUMBER(10, 0)", "INT") // INTEGER
    mapDBspecificTypeToStandard(ORACLE, "BLOB", "LONGBLOB") // MEDIUMBLOB
    mapDBspecificTypeToStandard(ORACLE, "CLOB", "MEDIUMTEXT")
    mapDBspecificTypeToStandard(ORACLE, "NUMBER(7, 0)", "MEDIUMINT")
    mapDBspecificTypeToStandard(ORACLE, "NUMBER", "NUMERIC") //YEAR
    mapDBspecificTypeToStandard(ORACLE, "NUMBER(5, 0)", "SMALLINT")
    mapDBspecificTypeToStandard(ORACLE, "BYTEA", "VARBINARY")
  }

  private fun createOracleToPostgresMapping() {
    addMapping(ORACLE, POSTGRESQL, "NUMBER", "NUMERIC")
  }

  private fun createH2SpecificMappings() {
    mapDBspecificTypeToStandard(H2DB, "LONGTEXT", "CLOB")
    mapDBspecificTypeToStandard(H2DB, "LONGBLOB", "BLOB")
    mapDBspecificTypeToStandard(H2DB, "BINARY LARGE OBJECT", "BLOB")
  }

  private fun createDerbySpecificMappings() {
    mapDBspecificTypeToStandard(DERBY, "LONGTEXT", "CLOB")
    mapDBspecificTypeToStandard(DERBY, "LONGBLOB", "BLOB")
  }

  private fun createMysqlToDB2Mapping() {
    addMapping(MYSQL, DB2, "LONGTEXT", "VARCHAR", "(4000)") //CHAR(254)
    addMapping(MYSQL, DB2, "LONGBLOB", "BLOB") //CLOB (2G)
    addMapping(MYSQL, DB2, "DECIMAL", "DECIMAL", "(16)") //CLOB (2G)
  }

  private fun createDB2ToMysqlMapping() {
    addMapping(DB2, MYSQL, "CLOB", "LONGBLOB")
    addMapping(DB2, MYSQL, "INTEGER", "INT", "(11)")
  }

  private fun createDB2ToPostgresMapping() {
    addMapping(DB2, POSTGRESQL, "BLOB", "BYTEA")
  }

  private fun createDB2ToMssqlMapping() {
    addMapping(DB2, MSSQL, "BLOB", "VARBINARY")
  }

  private fun createMysqlToPostgresMapping() {
    addMapping(MYSQL, POSTGRESQL, "BIGINT AUTO_INCREMENT", "BIGSERIAL")
    addMapping(MYSQL, POSTGRESQL, "BIGINT UNSIGNED", "NUMERIC", "(20)")
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
    addMapping(MYSQL, ORACLE, "VARBINARY", "BYTEA")
  }

  private fun createMysqlSpecificMappings() {
    mapDBspecificTypeToStandard(MYSQL, "YEAR", "NUMBER")
    mapDBspecificTypeToStandard(MYSQL, "SMALLINT UNSIGNED", "INTEGER")
    mapDBspecificTypeToStandard(MYSQL, "INTEGER UNSIGNED", "BIGINT")
    mapDBspecificTypeToStandard(MYSQL, "INT UNSIGNED", "BIGINT")
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
  }

  private fun createMssqlSpecificMappings() {
    mapDBspecificTypeToStandard(MSSQL, "NVARCHAR", "VARCHAR", "(255)")
    mapDBspecificTypeToStandard(MSSQL, "VARBINARY", "BLOB")
    mapDBspecificTypeToStandard(MSSQL, "BINARY", "BLOB")
    mapDBspecificTypeToStandard(MSSQL, "MONEY", "NUMBER", "(19,4)")
    mapDBspecificTypeToStandard(MSSQL, "SMALL DATETIME", "DATE")
    mapDBspecificTypeToStandard(MSSQL, "SMALL MONEY", "NUMBER", "(10,4)")
    mapDBspecificTypeToStandard(MSSQL, "UNIQUEIDENTIFIER", "CHAR", "(36)")
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
    addMapping(MSSQL, ORACLE, "NTEXT", "LONG")
    addMapping(MSSQL, ORACLE, "NVARCHAR", "NCHAR")
    addMapping(MSSQL, ORACLE, "NUMERIC", "NUMBER", "(10)")
    addMapping(MSSQL, ORACLE, "REAL", "FLOAT", "(23)")
    addMapping(MSSQL, ORACLE, "SMALLINT", "NUMBER", "(5)")
    addMapping(MSSQL, ORACLE, "TEXT", "LONG")
    addMapping(MSSQL, ORACLE, "TIMESTAMP", "DATETIME")
    addMapping(MSSQL, ORACLE, "TINYINT", "NUMBER", "(3)")
    addMapping(MSSQL, ORACLE, "VARBINARY", "RAW")
    addMapping(MSSQL, ORACLE, "VARCHAR", "VARCHAR2")
  }

  private fun createMssqlToPostgres() {
    addMapping(MSSQL, POSTGRESQL, "NVARCHAR", "TEXT")
    addMapping(MSSQL, POSTGRESQL, "VARBINARY", "BYTEA")
  }

  private fun mapDBspecificTypeToStandard(
    sourceDB: DatabaseType, sourceTypeName: String, targetTypeName: String, precision: String = ""
  ) {
    entries.forEach {
      if (it != sourceDB) {
        addMapping(sourceDB, it, sourceTypeName, targetTypeName, precision)
      }
    }
  }

  data class ColumnDefinition(val type: String, val precision: String)
}
