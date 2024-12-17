package io.github.guttenbase.mapping

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.DatabaseType.*
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import java.util.*

/**
 * Heuristic mapping of proprietary DB types
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
object ProprietaryColumnDefinitionResolver : ColumnDefinitionResolver {
  private val mappings = HashMap<DatabaseType, MutableMap<DatabaseType, MutableMap<String, TemplateColumnDefinition>>>()

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
    createDB2ToPostgresMapping()
  }

  override fun resolve(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnDefinition? {
    val columnType = column.columnTypeName.uppercase()
    val databaseMatrix = mappings[sourceDatabase.databaseType]

    if (databaseMatrix != null) {
      val databaseMapping = databaseMatrix[targetDatabase.databaseType]

      if (databaseMapping != null) {
        val mapping = databaseMapping[columnType]

        if (mapping != null) {
          return ColumnDefinition(column, mapping.targetType, mapping.precision, mapping.scale, mapping.usePrecision)
        }
      }
    }

    return null
  }

  @JvmOverloads
  fun addMapping(
    sourceDB: DatabaseType, targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String,
    precision: Int = 0, places: Int = 0
  ): ProprietaryColumnDefinitionResolver {
    addMappingInternal(sourceDB, targetDB, sourceTypeName, targetTypeName, precision, places)

    if (sourceDB === MYSQL) {
      addMappingInternal(MARIADB, targetDB, sourceTypeName, targetTypeName, precision, places)
    } else if (targetDB === MYSQL) {
      addMappingInternal(sourceDB, MARIADB, sourceTypeName, targetTypeName, precision, places)
    }

    return this
  }

  private fun addMappingInternal(
    sourceDB: DatabaseType, targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String,
    precision: Int, places: Int
  ) {
    val databaseMatrix = mappings.getOrPut(sourceDB) { EnumMap(DatabaseType::class.java) }
    val mapping = databaseMatrix.getOrPut(targetDB) { HashMap() }

    mapping[sourceTypeName] = TemplateColumnDefinition(targetTypeName, precision, places)
  }

  private fun createPostgresSpecificMappings() {
    mapDBspecificTypeToStandardType(POSTGRESQL, "TEXT", "VARCHAR", 4000) //CHAR(254)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BYTEA", "BLOB") //CLOB (2G) LONGBLOB
    mapDBspecificTypeToStandardType(POSTGRESQL, "NUMERIC", "DECIMAL", 16)
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT(2)", "SMALLINT", 16)
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT(4)", "SMALLINT", 16)
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT4", "INT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT2", "INT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT8", "TINYINT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "ARRAY", "LONGTEXT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "BIGSERIAL", "BIGINT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "BOX", "POLYGON")
    mapDBspecificTypeToStandardType(POSTGRESQL, "BYTEA", "LONGBLOB")
    mapDBspecificTypeToStandardType(POSTGRESQL, "CIDR", "VARCHAR(43)")
    mapDBspecificTypeToStandardType(POSTGRESQL, "CIRCLE", "POLYGON")
    mapDBspecificTypeToStandardType(POSTGRESQL, "DOUBLE PRECISION", "DOUBLE")
    mapDBspecificTypeToStandardType(POSTGRESQL, "INET", "VARCHAR", 43)
    mapDBspecificTypeToStandardType(POSTGRESQL, "INTERVAL", "TIME")
    mapDBspecificTypeToStandardType(POSTGRESQL, "JSON", "LONGTEXT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "LINE", "LINESTRING")
    mapDBspecificTypeToStandardType(POSTGRESQL, "LSEG", "LINESTRING")
    mapDBspecificTypeToStandardType(POSTGRESQL, "MACADDR", "VARCHAR", 17)
    mapDBspecificTypeToStandardType(POSTGRESQL, "MONEY", "DECIMAL", 19, 2)
    mapDBspecificTypeToStandardType(POSTGRESQL, "NATIONAL CHARACTER VARYING", "VARCHAR")
    mapDBspecificTypeToStandardType(POSTGRESQL, "NATIONAL CHARACTER", "CHAR")
    mapDBspecificTypeToStandardType(POSTGRESQL, "NUMERIC", "DECIMAL")
    mapDBspecificTypeToStandardType(POSTGRESQL, "PATH", "LINESTRING")
    mapDBspecificTypeToStandardType(POSTGRESQL, "REAL", "FLOAT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "SERIAL", "INT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "SMALLSERIAL", "SMALLINT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "TEXT", "LONGTEXT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "TIMESTAMP", "DATETIME")
    mapDBspecificTypeToStandardType(POSTGRESQL, "TSQUERY", "LONGTEXT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "TSVECTOR", "LONGTEXT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "TXID_SNAPSHOT", "VARCHAR")
    mapDBspecificTypeToStandardType(POSTGRESQL, "UUID", "VARCHAR", 36)
    mapDBspecificTypeToStandardType(POSTGRESQL, "XML", "LONGTEXT")
    mapDBspecificTypeToStandardType(POSTGRESQL, "BPCHAR", "CHAR", 1)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BPCHAR", "CHAR")
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT8", "NUMBER", 3)
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT4", "NUMBER", 5)
    mapDBspecificTypeToStandardType(POSTGRESQL, "INT16", "NUMBER", 10)
    mapDBspecificTypeToStandardType(POSTGRESQL, "BIGSERIAL", "NUMBER", 19)
    mapDBspecificTypeToStandardType(POSTGRESQL, "OID", "BLOB")
    mapDBspecificTypeToStandardType(POSTGRESQL, "BYTEA", "BINARY")
  }

  private fun createOracleSpecificMappings() {
    mapDBspecificTypeToStandardType(ORACLE, "VARCHAR2", "VARCHAR")
    mapDBspecificTypeToStandardType(ORACLE, "RAW", "BIT") //TINYBLOB
    mapDBspecificTypeToStandardType(ORACLE, "NUMBER(19, 0)", "BIGINT")
    mapDBspecificTypeToStandardType(ORACLE, "FLOAT(24)", "DECIMAL") // DOUBLE, DOUBLE PRECISION, REAL
    mapDBspecificTypeToStandardType(ORACLE, "NUMBER(10, 0)", "INT") // INTEGER
    mapDBspecificTypeToStandardType(ORACLE, "BLOB", "LONGBLOB") // MEDIUMBLOB
    mapDBspecificTypeToStandardType(ORACLE, "NUMBER", "NUMERIC") //YEAR
    mapDBspecificTypeToStandardType(ORACLE, "NUMBER(5, 0)", "SMALLINT")
  }

  private fun createH2SpecificMappings() {
    mapDBspecificTypeToStandardType(H2DB, "LONGTEXT", "CLOB")
    mapDBspecificTypeToStandardType(H2DB, "LONGBLOB", "BLOB")
    mapDBspecificTypeToStandardType(H2DB, "BINARY LARGE OBJECT", "BLOB")
  }

  private fun createDerbySpecificMappings() {
    mapDBspecificTypeToStandardType(DERBY, "LONGTEXT", "CLOB")
    mapDBspecificTypeToStandardType(DERBY, "LONGBLOB", "BLOB")
  }

  private fun createMysqlToDB2Mapping() {
    mapDBspecificTypeToStandardType(MYSQL, "LONGTEXT", "VARCHAR", 4000) //CHAR(254)
    mapDBspecificTypeToStandardType(MYSQL, "LONGBLOB", "BLOB") //CLOB (2G)
    mapDBspecificTypeToStandardType(MYSQL, "DECIMAL", "DECIMAL", 16) //CLOB (2G)
  }

  private fun createDB2ToPostgresMapping() {
    mapDBspecificTypeToStandardType(POSTGRESQL, "BLOB", "BYTEA")
  }

  private fun createMysqlToPostgresMapping() {
    addMapping(MYSQL, POSTGRESQL, "BIGINT AUTO_INCREMENT", "BIGSERIAL")
    addMapping(MYSQL, POSTGRESQL, "BIGINT UNSIGNED", "NUMERIC", 20)
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

  private fun createMysqlSpecificMappings() {
    mapDBspecificTypeToStandardType(MYSQL, "YEAR", "NUMBER")
    mapDBspecificTypeToStandardType(MYSQL, "SMALLINT UNSIGNED", "INTEGER")
    mapDBspecificTypeToStandardType(MYSQL, "INTEGER UNSIGNED", "BIGINT")
    mapDBspecificTypeToStandardType(MYSQL, "INT UNSIGNED", "BIGINT")
  }

  private fun createMssqlSpecificMappings() {
    mapDBspecificTypeToStandardType(MSSQL, "NVARCHAR", "VARCHAR", 255)
    mapDBspecificTypeToStandardType(MSSQL, "VARBINARY", "BLOB")
    mapDBspecificTypeToStandardType(MSSQL, "BINARY", "BLOB")
    mapDBspecificTypeToStandardType(MSSQL, "MONEY", "NUMBER", 19, 4)
    mapDBspecificTypeToStandardType(MSSQL, "SMALL DATETIME", "DATE")
    mapDBspecificTypeToStandardType(MSSQL, "SMALL MONEY", "NUMBER", 10, 42)
    mapDBspecificTypeToStandardType(MSSQL, "UNIQUEIDENTIFIER", "CHAR", 36)

    mapStandardTypeToDBspecificType(MSSQL, "BOOLEAN", "BIT")
  }

  private fun mapDBspecificTypeToStandardType(
    sourceDB: DatabaseType, sourceTypeName: String, targetTypeName: String,
    precision: Int = 0, places: Int = 0
  ) {
    entries.forEach {
      if (it != sourceDB) {
        addMapping(sourceDB, it, sourceTypeName, targetTypeName, precision, places)
      }
    }
  }

  private fun mapStandardTypeToDBspecificType(
    targetDB: DatabaseType, sourceTypeName: String, targetTypeName: String,
    precision: Int = 0, places: Int = 0
  ) {
    entries.forEach {
      if (it != targetDB) {
        addMapping(it, targetDB, sourceTypeName, targetTypeName, precision, places)
      }
    }
  }
}

private data class TemplateColumnDefinition(val targetType: String, val precision: Int = 0, val scale: Int = 0) {
  val usePrecision = precision > 0
}
