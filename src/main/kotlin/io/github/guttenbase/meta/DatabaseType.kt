package io.github.guttenbase.meta

import io.github.guttenbase.mapping.ColumnTypeDefinition
import io.github.guttenbase.repository.hint
import io.github.guttenbase.schema.AutoIncrementValue
import java.sql.JDBCType.*
import java.sql.Types

// Placeholders to be replaced in templates
private const val HEX_VALUE = "@@HEX_VALUE@@"
private const val NEXT_VALUE = "@@NEXT_VALUE@@"
private const val STEP_VALUE = "@@STEP_VALUE@@"
private const val TABLE_NAME = "@@TABLE_NAME@@"
private const val COLUMN_NAME = "@@COLUMN_NAME@@"

/**
 * Bundle knowledge about well-known data bases
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("MemberVisibilityCanBePrivate")
enum class DatabaseType(
  /**
   * ID columns may be defined as autoincremented, i.e. every time data is inserted the ID will be incremented autoimatically.
   * Unfortunately every database has its own way to implement this feature.
   */
  private val autoincrementColumnClause: String,
  private val autoincrementColumnStatement: String? = null
) {
  // Dummy value
  GENERIC("IDENTITY($NEXT_VALUE, 1)"),
  MOCK("IDENTITY($NEXT_VALUE, 1)"),
  EXPORT_DUMP("IDENTITY($NEXT_VALUE, 1)"),
  IMPORT_DUMP("IDENTITY($NEXT_VALUE, 1)"),

  MYSQL("AUTO_INCREMENT", "ALTER TABLE $TABLE_NAME AUTO_INCREMENT = $NEXT_VALUE;"),
  MARIADB("AUTO_INCREMENT", "ALTER TABLE $TABLE_NAME AUTO_INCREMENT = $NEXT_VALUE;"),

  POSTGRESQL(
    "", // Handled by column type, see below)
    "SELECT setval('${TABLE_NAME}_id_seq', $NEXT_VALUE, true);"
  ),
  MSSQL("IDENTITY($NEXT_VALUE, 1)"),

  MS_ACCESS("AUTOINCREMENT($NEXT_VALUE, 1)"),

  DB2("GENERATED BY DEFAULT AS IDENTITY (START WITH $NEXT_VALUE, INCREMENT BY $STEP_VALUE)"),

  SYBASE("IDENTITY", "CALL sa_reset_identity('$TABLE_NAME', 'DBA', $NEXT_VALUE - 1);"),

  ORACLE("GENERATED BY DEFAULT ON NULL AS IDENTITY START WITH $NEXT_VALUE INCREMENT BY $STEP_VALUE"),

  HSQLDB("GENERATED BY DEFAULT AS IDENTITY (START WITH $NEXT_VALUE, INCREMENT BY $STEP_VALUE)"), // IDENTITY
  H2DB(
    "GENERATED BY DEFAULT AS IDENTITY",
    "ALTER TABLE $TABLE_NAME ALTER COLUMN $COLUMN_NAME RESTART WITH $NEXT_VALUE;"
  ), // AUTO_INCREMENT
  DERBY("GENERATED BY DEFAULT AS IDENTITY (START WITH $NEXT_VALUE, INCREMENT BY $STEP_VALUE)")
  ;

  /**
   * To be executed when table DDL script is created
   */
  fun createColumnAutoIncrementType(column: ColumnMetaData): DatabaseSupportedColumnType? {
    assert(column.isAutoIncrement) { "$column is no auto increment column" }

    return when (this) {
      POSTGRESQL -> DatabaseSupportedColumnType(
        when (column.columnType) {
          Types.BIGINT -> "BIGSERIAL"
          Types.INTEGER -> "SERIAL"
          Types.SMALLINT -> "SMALLSERIAL"
          else -> "SERIAL"
        }, column.jdbcColumnType
      )

      else -> null
    }
  }

  /**
   * ID columns may be defined as autoincremented, i.e. every time data is inserted the ID will be incremented autoimatically.
   * Unfortunately every database has its own way to implement this feature.
   *
   * @return the autoincrement clause for the target database
   */
  fun createColumnAutoincrementClause(column: ColumnMetaData): String {
    assert(column.isAutoIncrement) { "$column is no auto increment column" }

    val autoIncrementValue = retrieveAutoIncrementValue(column)
    val startValue = autoIncrementValue.startValue(column)
    val stepValue = autoIncrementValue.stepWidth(column)

    return autoincrementColumnClause.replace(NEXT_VALUE, startValue.toString())
      .replace(STEP_VALUE, stepValue.toString())
      .replace(TABLE_NAME, column.tableMetaData.tableName).replace(COLUMN_NAME, column.columnName)
  }

  fun createDefaultValueClause(columnDefinition: ColumnTypeDefinition): String? =
    if (this == MYSQL && columnDefinition.typeName.equals("TIMESTAMP", true)) {
      " DEFAULT CURRENT_TIMESTAMP"    // Otherwise may result in [42000][1067] Invalid default value for 'CREATED_AT'
    } else null

  /**
   * To be executed after table DDL script has run (optionally)
   */
  fun createColumnAutoincrementStatement(column: ColumnMetaData): String? {
    assert(column.isAutoIncrement) { "$column is no auto increment column" }

    return if (autoincrementColumnStatement != null) {
      val autoIncrementValue = retrieveAutoIncrementValue(column)
      val startValue = autoIncrementValue.startValue(column)
      val stepValue = autoIncrementValue.stepWidth(column)

      autoincrementColumnStatement.replace(NEXT_VALUE, startValue.toString())
        .replace(STEP_VALUE, stepValue.toString())
        .replace(TABLE_NAME, column.tableMetaData.tableName).replace(COLUMN_NAME, column.columnName)
    } else {
      null
    }
  }

  /**
   * @return clause to print before the hex-encoded data
   */
  val blobDataPrefix: String
    get() {
      val clause = blobDataClause

      return clause.substring(0, clause.indexOf(HEX_VALUE))
    }

  /**
   * @return clause to print after the hex-encoded data
   */
  val blobDataSuffix: String
    get() {
      val clause = blobDataClause

      return clause.substring(clause.indexOf(HEX_VALUE) + HEX_VALUE.length)
    }

  val dropTablesSuffix: String
    get() = when (this) {
      H2DB, HSQLDB, POSTGRESQL, MARIADB, ORACLE -> "CASCADE"
      else -> ""
    }

  /**
   * @return clause with template variable to be replaced with actual data
   */
  val blobDataClause: String
    get() = when (this) {
      POSTGRESQL -> """E'\\x$HEX_VALUE'"""
      SYBASE, MSSQL, MYSQL, MARIADB -> """0x$HEX_VALUE"""
      ORACLE -> """'$HEX_VALUE'"""
      DB2 -> """BLOB(x'$HEX_VALUE')"""
      H2DB, HSQLDB, DERBY -> """CAST (X'$HEX_VALUE' AS BLOB)"""
      else -> """CAST (X'$HEX_VALUE' AS BLOB)""" // Hope for the best...
    }

  /**
   * @return escape charcter to wrap column or table names, e.g. if they contain special characters or key words
   */
  val escapeCharacter: String
    get() = when (this) {
      MYSQL, MARIADB -> "`"
      else -> "\""
    }

  val tableExistsClause: String
    get() = when (this) {
      H2DB, HSQLDB, POSTGRESQL, MARIADB, MYSQL -> "IF EXISTS"
      else -> ""
    }

  val indexExistsClause: String
    get() = when (this) {
      H2DB, HSQLDB, POSTGRESQL -> "IF EXISTS"
      else -> ""
    }

  val constraintExistsClause: String
    get() = when (this) {
      H2DB, POSTGRESQL -> "IF EXISTS"
      else -> ""
    }

  val tableNotExistsClause: String
    get() = when (this) {
      H2DB, HSQLDB, POSTGRESQL, MYSQL -> "IF NOT EXISTS"
      else -> ""
    }

  @JvmOverloads
  fun escapeDatabaseEntity(name: String, prefix: String = "") = prefix + escapeCharacter + name + escapeCharacter
//
//  fun supportsPrecisionClause(type: JDBCType) =
//    type.isStringType() || type.isNumericType() || type.isBlobType() || type.isBinaryType()

  fun supportsPrecisionClause(type: String) =
    type == VARCHAR.name || type == CHAR.name || type == DECIMAL.name || type == NUMERIC.name || type == BINARY.name

  private fun retrieveAutoIncrementValue(column: ColumnMetaData): AutoIncrementValue {
    val connectorRepository = column.tableMetaData.databaseMetaData.connectorRepository
    val connectorId = column.tableMetaData.databaseMetaData.connectorId

    return connectorRepository.hint<AutoIncrementValue>(connectorId)
  }
}