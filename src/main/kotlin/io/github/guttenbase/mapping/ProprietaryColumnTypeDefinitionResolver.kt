package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseColumnType
import java.sql.JDBCType
import java.util.*

/**
 * Heuristic mapping of proprietary DB types
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
object ProprietaryColumnTypeDefinitionResolver : ColumnTypeDefinitionResolver {
  private val mappings = HashMap<DatabaseType, MutableMap<DatabaseType, MutableMap<String, DatabaseColumnType>>>()

  init {
    // DB data types to Standard SQL, if possible
    createH2SpecificMappings()
    createDerbySpecificMappings()
    createPostgresSpecificMappings()
    createOracleSpecificMappings()
    createMssqlSpecificMappings()

    createMysqlSpecificMappings()
    createDB2Mappings()
  }

  override fun resolve(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnTypeDefinition? {
    val columnType = column.columnTypeName.uppercase()
    val databaseMatrix = mappings[sourceDatabase.databaseType]

    if (databaseMatrix != null) {
      val databaseMapping = databaseMatrix[targetDatabase.databaseType]

      if (databaseMapping != null) {
        val mapping = databaseMapping[columnType]

        if (mapping != null) {
          val precision = computePrecision(column, mapping)

          return ColumnTypeDefinition(column, mapping.typeName, precision, column.scale)
        }
      }
    }

    return null
  }

  @JvmOverloads
  fun addMapping(
    sourceDB: DatabaseType, targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String,
    type: JDBCType, maxPrecision: Int = 0, places: Int = 0
  ): ProprietaryColumnTypeDefinitionResolver {
    addMappingInternal(sourceDB, targetDB, sourceTypeName, targetTypeName, type, maxPrecision, places)

    if (sourceDB === MYSQL) {
      addMappingInternal(MARIADB, targetDB, sourceTypeName, targetTypeName, type, maxPrecision, places)
    } else if (targetDB === MYSQL) {
      addMappingInternal(sourceDB, MARIADB, sourceTypeName, targetTypeName, type, maxPrecision, places)
    }

    return this
  }

  private fun addMappingInternal(
    sourceDB: DatabaseType, targetDB: DatabaseType,
    sourceTypeName: String, targetTypeName: String, type: JDBCType,
    maxPrecision: Int, places: Int
  ) {
    val databaseMatrix = mappings.getOrPut(sourceDB) { EnumMap(DatabaseType::class.java) }
    val mapping = databaseMatrix.getOrPut(targetDB) { HashMap() }

    mapping[sourceTypeName] = DatabaseColumnType(targetTypeName, type, maxPrecision, places)
  }

  private fun createPostgresSpecificMappings() {
    mapDBspecificTypeToStandardType(POSTGRESQL, "TEXT", "VARCHAR", JDBCType.CLOB, 4000) //CHAR(254)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BYTEA", "BLOB", JDBCType.BLOB) //CLOB (2G) LONGBLOB
    mapDBspecificTypeToStandardType(POSTGRESQL, "NUMERIC", "DECIMAL", JDBCType.NUMERIC, 16)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BIGSERIAL", "BIGINT", JDBCType.BIGINT)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BYTEA", "LONGBLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(POSTGRESQL, "DOUBLE PRECISION", "DOUBLE", JDBCType.DOUBLE)
    mapDBspecificTypeToStandardType(POSTGRESQL, "SMALLSERIAL", "SMALLINT", JDBCType.SMALLINT)
    mapDBspecificTypeToStandardType(POSTGRESQL, "TEXT", "LONGTEXT", JDBCType.CLOB)
    mapDBspecificTypeToStandardType(POSTGRESQL, "UUID", "VARCHAR", JDBCType.VARCHAR, 36)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BPCHAR", "CHAR", JDBCType.CHAR)
    mapDBspecificTypeToStandardType(POSTGRESQL, "OID", "BLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BYTEA", "BLOB", JDBCType.BLOB)

    mapStandardTypeToDBspecificType(POSTGRESQL, "VARBINARY", "BYTEA", JDBCType.VARBINARY)
    mapStandardTypeToDBspecificType(POSTGRESQL, "BLOB", "BYTEA", JDBCType.BLOB)
    mapStandardTypeToDBspecificType(POSTGRESQL, "CLOB", "TEXT", JDBCType.LONGVARCHAR)
  }

  private fun createOracleSpecificMappings() {
    mapDBspecificTypeToStandardType(ORACLE, "VARCHAR2", "VARCHAR", JDBCType.VARCHAR)
    mapDBspecificTypeToStandardType(ORACLE, "RAW", "BIT", JDBCType.BIT) //TINYBLOB
    mapDBspecificTypeToStandardType(ORACLE, "NUMBER(19, 0)", "BIGINT", JDBCType.BIGINT)

    mapStandardTypeToDBspecificType(ORACLE, "BINARY", "RAW", JDBCType.BINARY, 4000)
    mapStandardTypeToDBspecificType(ORACLE, "DOUBLE", "DOUBLE PRECISION", JDBCType.DOUBLE)
  }

  private fun createH2SpecificMappings() {
    mapDBspecificTypeToStandardType(H2DB, "LONGTEXT", "CLOB", JDBCType.CLOB)
    mapDBspecificTypeToStandardType(H2DB, "LONGBLOB", "BLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(H2DB, "BINARY LARGE OBJECT", "BLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(HSQLDB, "CHARACTER", "CHAR", JDBCType.CHAR)
  }

  private fun createDerbySpecificMappings() {
    mapDBspecificTypeToStandardType(DERBY, "LONGTEXT", "CLOB", JDBCType.CLOB)
    mapDBspecificTypeToStandardType(DERBY, "LONGBLOB", "BLOB", JDBCType.BLOB)
  }

  private fun createDB2Mappings() {
    mapStandardTypeToDBspecificType(DB2, "NUMBER", "DECIMAL", JDBCType.DECIMAL, 31, 5)
  }

  private fun createMysqlSpecificMappings() {
    mapDBspecificTypeToStandardType(MYSQL, "LONGTEXT", "VARCHAR", JDBCType.VARCHAR, 2147483647)
    mapDBspecificTypeToStandardType(MYSQL, "MEDIUMTEXT", "VARCHAR", JDBCType.VARCHAR, 16777215)
    mapDBspecificTypeToStandardType(MYSQL, "LONGBLOB", "BLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(MYSQL, "DECIMAL", "DECIMAL", JDBCType.DECIMAL, 16)
    mapDBspecificTypeToStandardType(MYSQL, "YEAR", "NUMBER", JDBCType.NUMERIC)
    mapDBspecificTypeToStandardType(MYSQL, "SMALLINT UNSIGNED", "INTEGER", JDBCType.INTEGER)
    mapDBspecificTypeToStandardType(MYSQL, "INTEGER UNSIGNED", "BIGINT", JDBCType.BIGINT)
    mapDBspecificTypeToStandardType(MYSQL, "INT UNSIGNED", "BIGINT", JDBCType.BIGINT)
    mapStandardTypeToDBspecificType(MYSQL, "NUMBER", "NUMERIC", JDBCType.NUMERIC, 65)

    mapStandardTypeToDBspecificType(MYSQL, "CLOB", "LONGTEXT", JDBCType.LONGVARCHAR)
//    mapStandardTypeToDBspecificType(MYSQL, "BLOB", "LONGTEXT", JDBCType.LONGVARCHAR, 2147483647)
  }

  private fun createMssqlSpecificMappings() {
    mapDBspecificTypeToStandardType(MSSQL, "NVARCHAR", "VARCHAR", JDBCType.VARCHAR, 255)
    mapDBspecificTypeToStandardType(MSSQL, "VARBINARY", "BLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(MSSQL, "BINARY", "BLOB", JDBCType.BLOB)
    mapDBspecificTypeToStandardType(MSSQL, "MONEY", "NUMBER", JDBCType.NUMERIC, 19, 4)
    mapDBspecificTypeToStandardType(MSSQL, "SMALL DATETIME", "DATE", JDBCType.DATE)
    mapDBspecificTypeToStandardType(MSSQL, "SMALL MONEY", "NUMBER", JDBCType.NUMERIC, 10, 42)
    mapDBspecificTypeToStandardType(MSSQL, "UNIQUEIDENTIFIER", "CHAR", JDBCType.CHAR, 36)

    mapStandardTypeToDBspecificType(MSSQL, "BOOLEAN", "BIT", JDBCType.BOOLEAN)
    mapStandardTypeToDBspecificType(MSSQL, "BLOB", "IMAGE", JDBCType.BLOB)
    mapStandardTypeToDBspecificType(MSSQL, "CLOB", "VARCHAR(MAX)", JDBCType.LONGVARCHAR) // TEXT
  }

  private fun mapDBspecificTypeToStandardType(
    sourceDB: DatabaseType, sourceTypeName: String, targetTypeName: String, type: JDBCType,
    maxPrecision: Int = 0, places: Int = 0
  ) {
    entries.forEach {
      if (it != sourceDB) {
        addMapping(sourceDB, it, sourceTypeName, targetTypeName, type, maxPrecision, places)
      }
    }
  }

  private fun mapStandardTypeToDBspecificType(
    targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String, type: JDBCType,
    maxPrecision: Int = 0, places: Int = 0
  ) {
    entries.forEach {
      if (it != targetDB) {
        addMapping(it, targetDB, sourceTypeName, targetTypeName, type, maxPrecision, places)
      }
    }
  }
}
