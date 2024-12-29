package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseColumnType
import java.sql.JDBCType
import java.sql.JDBCType.*
import java.util.*

/**
 * Try to resolve types using heuristic mapping of proprietary DB types
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
object ProprietaryColumnTypeDefinitionResolver : ColumnTypeDefinitionResolver {
  private val mappings =
    HashMap<DatabaseType, MutableMap<DatabaseType, MutableMap<String, ColumnTypeDefinitionResolver>>>()

  init {
    // DB data types to Standard SQL, if possible
    createH2SpecificMappings()
    createHSQLDBSpecificMappings()
    createDerbySpecificMappings()
    createPostgresSpecificMappings()
    createOracleSpecificMappings()
    createMssqlSpecificMappings()

    createMysqlSpecificMappings()
    createDB2Mappings()
  }

  /**
   * Resolve column type definition for given column and database type by lookup in specified matrix of [ColumnTypeDefinitionResolver]s.
   */
  override fun resolve(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnTypeDefinition? {
    val columnType = column.columnTypeName.uppercase()
    val databaseMatrix = mappings[sourceDatabase.databaseType]

    if (databaseMatrix != null) {
      val databaseMapping = databaseMatrix[targetDatabase.databaseType]

      if (databaseMapping != null) {
        val resolver = databaseMapping[columnType]

        if (resolver != null) {
          return resolver.resolve(sourceDatabase, targetDatabase, column)
        }
      }
    }

    return null
  }

  private fun addMapping(
    sourceDB: DatabaseType, targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String,
    type: JDBCType, maxPrecision: Int = -1, places: Int = -1
  ) {
    addSimpleMapping(sourceDB, targetDB, sourceTypeName, targetTypeName, type, maxPrecision, places)

    if (sourceDB === MYSQL) {
      addSimpleMapping(MARIADB, targetDB, sourceTypeName, targetTypeName, type, maxPrecision, places)
    } else if (targetDB === MYSQL) {
      addSimpleMapping(sourceDB, MARIADB, sourceTypeName, targetTypeName, type, maxPrecision, places)
    }
  }

  private fun addSimpleMapping(
    sourceDB: DatabaseType, targetDB: DatabaseType,
    sourceTypeName: String, targetTypeName: String, type: JDBCType,
    maxPrecision: Int, places: Int
  ) {
    val resolver = ColumnTypeDefinitionResolver { sourceDatabase, targetDatabase, column ->
      val precision = computePrecision(column, DatabaseColumnType(targetTypeName, type, maxPrecision, places))
      ColumnTypeDefinition(column, targetTypeName, precision, column.scale)
    }

    addMappingInternal(sourceDB, targetDB, sourceTypeName, resolver)
  }

  private fun addMappingInternal(
    sourceDB: DatabaseType, targetDB: DatabaseType, sourceTypeName: String, resolver: ColumnTypeDefinitionResolver
  ) {
    val databaseMatrix = mappings.getOrPut(sourceDB) { EnumMap(DatabaseType::class.java) }
    val mapping = databaseMatrix.getOrPut(targetDB) { HashMap() }

    mapping[sourceTypeName] = resolver
  }

  /**
   * Add mapping for source type to all other database types
   */
  @Suppress("SameParameterValue")
  private fun addSourceTypeMapping(
    sourceDB: DatabaseType, sourceTypeName: String, resolver: ColumnTypeDefinitionResolver
  ) {
    DatabaseType.entries.forEach {
      if (it != sourceDB) {
        addMappingInternal(sourceDB, it, sourceTypeName, resolver)
      }
    }
  }

  /**
   * Add mapping for target type from other database types
   */
  @Suppress("SameParameterValue")
  private fun addTargetTypeMapping(
    targetDB: DatabaseType, targetTypeName: String, resolver: ColumnTypeDefinitionResolver
  ) {
    DatabaseType.entries.forEach {
      if (it != targetDB) {
        addMappingInternal(it, targetDB, targetTypeName, resolver)
      }
    }
  }

  /**
   * Add mapping for source type to all other database types
   */
  private fun addSourceTypeMapping(
    sourceDB: DatabaseType, sourceTypeName: String, targetTypeName: String, type: JDBCType,
    maxPrecision: Int = -1, places: Int = -1
  ) {
    DatabaseType.entries.forEach {
      if (it != sourceDB) {
        addMapping(sourceDB, it, sourceTypeName, targetTypeName, type, maxPrecision, places)
      }
    }
  }

  /**
   * Add mapping for target type from other database types
   */
  private fun addTargetTypeMapping(
    targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String, type: JDBCType,
    maxPrecision: Int = 0, places: Int = 0
  ) {
    DatabaseType.entries.forEach {
      if (it != targetDB) {
        addMapping(it, targetDB, sourceTypeName, targetTypeName, type, maxPrecision, places)
      }
    }
  }

  private fun createPostgresSpecificMappings() {
//    addSourceTypeMapping(POSTGRESQL, "TEXT", "VARCHAR", CLOB, 4000) //CHAR(254)
//    addSourceTypeMapping(POSTGRESQL, "TEXT", "LONGTEXT", CLOB)
    addSourceTypeMapping(POSTGRESQL, "BYTEA", "BLOB", BLOB) //CLOB (2G) LONGBLOB
    addSourceTypeMapping(POSTGRESQL, "NUMERIC", "DECIMAL", NUMERIC, 16)
    addSourceTypeMapping(POSTGRESQL, "BIGSERIAL", "BIGINT", BIGINT)
    addSourceTypeMapping(POSTGRESQL, "BYTEA", "LONGBLOB", BLOB)
    addSourceTypeMapping(POSTGRESQL, "DOUBLE PRECISION", "DOUBLE", DOUBLE)
    addSourceTypeMapping(POSTGRESQL, "SMALLSERIAL", "SMALLINT", SMALLINT)
    addSourceTypeMapping(POSTGRESQL, "UUID", "VARCHAR", VARCHAR, 36)
    addSourceTypeMapping(POSTGRESQL, "BPCHAR", "CHAR", CHAR)
    addSourceTypeMapping(POSTGRESQL, "OID", "BLOB", BLOB)

    addTargetTypeMapping(POSTGRESQL, "VARBINARY", "BYTEA", VARBINARY)
    addTargetTypeMapping(POSTGRESQL, "BLOB", "BYTEA", BLOB)
    addTargetTypeMapping(POSTGRESQL, "CLOB", "TEXT", LONGVARCHAR)
  }

  private fun createOracleSpecificMappings() {
    addSourceTypeMapping(ORACLE, "VARCHAR2", "VARCHAR", VARCHAR)
    addSourceTypeMapping(ORACLE, "RAW", "BIT", BIT) //TINYBLOB

//    addSourceTypeMapping(ORACLE, "NUMBER", "DECIMAL", DECIMAL)
    addSourceTypeMapping(ORACLE, "NUMBER", "BIGINT", BIGINT, 0, 0)
//    addMapping(ORACLE, H2DB, "NUMBER", "BIGINT", BIGINT, 0, 0) // H2 does not support BIGINT with precision

    // For some reason, Oracle return JDBC type TIMESTAMP for DATE columns??
    addSourceTypeMapping(ORACLE, "DATE") { _, _, column ->
      if (column.jdbcColumnType == TIMESTAMP) ColumnTypeDefinition(column, "DATE") else null
    }

    addTargetTypeMapping(ORACLE, "BINARY", "RAW", BINARY, 4000)
    addTargetTypeMapping(ORACLE, "DOUBLE", "DOUBLE PRECISION", DOUBLE)
    addTargetTypeMapping(ORACLE, "BLOB", "BLOB", BLOB)
    addTargetTypeMapping(ORACLE, "CLOB", "CLOB", CLOB)
  }

  private fun createH2SpecificMappings() {
    addSourceTypeMapping(H2DB, "LONGTEXT", "CLOB", CLOB)
    addSourceTypeMapping(H2DB, "LONGBLOB", "BLOB", BLOB)
    addSourceTypeMapping(H2DB, "BINARY LARGE OBJECT", "BLOB", BLOB)
  }

  private fun createHSQLDBSpecificMappings() {
    addSourceTypeMapping(HSQLDB, "CHARACTER", "CHAR", CHAR)
  }

  private fun createDerbySpecificMappings() {
    addSourceTypeMapping(DERBY, "LONGTEXT", "CLOB", CLOB)
    addSourceTypeMapping(DERBY, "LONGBLOB", "BLOB", BLOB)

//    addMapping(ORACLE, DERBY, "NUMBER", "BIGINT", BIGINT, 0, 0) // DERBY does not support BIGINT with precision
  }

  // https://www.ibm.com/docs/en/iis/11.5?topic=dts-db2-data-type-support
  private fun createDB2Mappings() {
    addTargetTypeMapping(DB2, "NUMBER", "DECIMAL", DECIMAL, 31, 5)
    addTargetTypeMapping(DB2, "VARCHAR") { _, _, column ->
      if (column.jdbcColumnType == VARCHAR && column.precision > 32000)
        ColumnTypeDefinition(column, "CLOB") else null
    }
    addTargetTypeMapping(DB2, "VARBINARY") { _, _, column ->
      if (column.jdbcColumnType == VARBINARY && column.precision > 32000)
        ColumnTypeDefinition(column, "BLOB") else null
    }
  }

  private fun createMysqlSpecificMappings() {
    addSourceTypeMapping(MYSQL, "LONGTEXT", "CLOB", CLOB, 0, 0)
    addSourceTypeMapping(MYSQL, "MEDIUMTEXT", "CLOB", CLOB, 0, 0)
    addSourceTypeMapping(MYSQL, "TEXT", "CLOB", CLOB, 0, 0)
    addSourceTypeMapping(MYSQL, "MEDIUMBLOB", "BLOB", BLOB, 0, 0)
    addSourceTypeMapping(MYSQL, "LONGBLOB", "BLOB", BLOB, 0, 0)
    addSourceTypeMapping(MYSQL, "MEDIUMINT UNSIGNED", "INTEGER", INTEGER)
    addSourceTypeMapping(MYSQL, "TINYINT UNSIGNED", "INTEGER", INTEGER)
    addSourceTypeMapping(MYSQL, "SMALLINT UNSIGNED", "INTEGER", INTEGER)
    addSourceTypeMapping(MYSQL, "INTEGER UNSIGNED", "BIGINT", BIGINT)
    addSourceTypeMapping(MYSQL, "INT UNSIGNED", "BIGINT", BIGINT)
    addSourceTypeMapping(MYSQL, "YEAR", "INTEGER", INTEGER)
//    addSourceTypeMapping(MYSQL, "UUID", "VARCHAR", VARCHAR, 36)

    addTargetTypeMapping(MYSQL, "CLOB", "LONGTEXT", LONGVARCHAR)
    addTargetTypeMapping(MYSQL, "NUMBER", "NUMERIC", NUMERIC, 65)

    addTargetTypeMapping(MYSQL, "VARCHAR") { _, _, column ->
      if (column.jdbcColumnType == VARCHAR && column.precision > 16300)
        ColumnTypeDefinition(column, "TEXT", 0, 0) else null
    }
    addTargetTypeMapping(MYSQL, "VARBINARY") { _, _, column ->
      if (column.jdbcColumnType == VARBINARY && column.precision > 65000)
        ColumnTypeDefinition(column, "BLOB", 0, 0) else null
    }
  }

  private fun createMssqlSpecificMappings() {
    addSourceTypeMapping(MSSQL, "NVARCHAR", "VARCHAR", VARCHAR, 255)
//    addSourceTypeMapping(MSSQL, "VARBINARY", "BLOB", BLOB)
//    addSourceTypeMapping(MSSQL, "BINARY", "BLOB", BLOB)
    addSourceTypeMapping(MSSQL, "MONEY", "NUMBER", NUMERIC, 19, 4)
    addSourceTypeMapping(MSSQL, "SMALL DATETIME", "DATE", DATE)
    addSourceTypeMapping(MSSQL, "SMALL MONEY", "NUMBER", NUMERIC, 10, 42)
    addSourceTypeMapping(MSSQL, "UNIQUEIDENTIFIER", "CHAR", CHAR, 36)

//    addMapping(ORACLE, MSSQL, "NUMBER", "BIGINT", BIGINT, 0, 0) // MSSQL does not support BIGINT with precision

    addTargetTypeMapping(MSSQL, "BOOLEAN", "BIT", BOOLEAN)
    addTargetTypeMapping(MSSQL, "BLOB", "IMAGE", BLOB)
    addTargetTypeMapping(MSSQL, "CLOB", "VARCHAR(MAX)", LONGVARCHAR) // TEXT
  }
}
