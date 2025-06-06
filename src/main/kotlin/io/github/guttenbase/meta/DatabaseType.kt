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
private const val VIEW_NAME = "@@VIEW_NAME@@"
private const val COLUMN_NAME = "@@COLUMN_NAME@@"

private const val COMMON_SELECT_VIEWDEFINITION = "SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '$VIEW_NAME'"

/**
 * Bundle knowledge about well-known data bases
 *
 * &copy; 2012-2044 akquinet tech@spree
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
  private val autoincrementColumnStatement: String?,
  private val selectViewDefinitionClause: String,
  private val escapeDatabaseEntities: Boolean = true
) {
  // Dummy value
  GENERIC(
    "IDENTITY($NEXT_VALUE, 1)", null, COMMON_SELECT_VIEWDEFINITION
  ),
  MOCK("IDENTITY($NEXT_VALUE, 1)", null, COMMON_SELECT_VIEWDEFINITION),

  MYSQL("AUTO_INCREMENT", "ALTER TABLE $TABLE_NAME AUTO_INCREMENT = $NEXT_VALUE;", COMMON_SELECT_VIEWDEFINITION),
  MARIADB(
    "AUTO_INCREMENT", "ALTER TABLE $TABLE_NAME AUTO_INCREMENT = $NEXT_VALUE;", COMMON_SELECT_VIEWDEFINITION
  ),

  POSTGRESQL(
    "", // Handled by column type, see below)
    """SELECT setval('"${TABLE_NAME}_${COLUMN_NAME}_seq"', $NEXT_VALUE, true);""",
    "SELECT pg_get_viewdef('$VIEW_NAME', true)",
    false
  ),


  MSSQL(
    "IDENTITY($NEXT_VALUE, 1)", null, "sp_helptext '$VIEW_NAME'"
  ),

  MS_ACCESS("AUTOINCREMENT($NEXT_VALUE, 1)", null, """
  SELECT definition FROM sys.objects o 
  INNER JOIN sys.sql_modules m ON m.object_id = o.object_id
  WHERE o.object_id = object_id( 'dbo.$VIEW_NAME')
    AND o.type = 'V'
    """.trimIndent()),

  IBMDB2("GENERATED BY DEFAULT AS IDENTITY (START WITH $NEXT_VALUE, INCREMENT BY $STEP_VALUE)", null,
    "SELECT TEXT FROM SYSIBM.SYSVIEWS WHERE NAME = '$VIEW_NAME'"),

  SYBASE("IDENTITY", "CALL sa_reset_identity('$TABLE_NAME', 'DBA', $NEXT_VALUE - 1);", "sp_helptext '$VIEW_NAME'"),

  // https://stackoverflow.com/questions/11296361/how-to-create-id-with-auto-increment-on-oracle
  ORACLE(
    "GENERATED BY DEFAULT ON NULL AS IDENTITY (START WITH $NEXT_VALUE INCREMENT BY $STEP_VALUE)", null,
    "SELECT TEXT FROM \"PUBLIC\".ALL_VIEWS WHERE VIEW_NAME = '$VIEW_NAME'"
  ),

  HSQLDB(
    "GENERATED BY DEFAULT AS IDENTITY (START WITH $NEXT_VALUE, INCREMENT BY $STEP_VALUE)", null, COMMON_SELECT_VIEWDEFINITION
  ), // IDENTITY

  H2DB(
    "GENERATED BY DEFAULT AS IDENTITY",
    "ALTER TABLE $TABLE_NAME ALTER COLUMN $COLUMN_NAME RESTART WITH $NEXT_VALUE;", COMMON_SELECT_VIEWDEFINITION
  ),

  DERBY(
    "GENERATED BY DEFAULT AS IDENTITY (START WITH $NEXT_VALUE, INCREMENT BY $STEP_VALUE)", null, """
      SELECT VIEWDEFINITION FROM SYS.SYSVIEWS v INNER JOIN SYS.SYSTABLES t ON v.TABLEID = t.TABLEID
      WHERE t.TABLENAME = '$VIEW_NAME'
      """
  )
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
      .replace(TABLE_NAME, escapeDatabaseEntity(column.table.tableName))
      .replace(COLUMN_NAME, escapeDatabaseEntity(column.columnName))
  }

  /**
   * Most databases allow to query the view definition SQL statement.
   *
   * @return SQL statement to create view definition, if available
   */
  fun createViewDefinitionClause(view: ViewMetaData) = selectViewDefinitionClause.replace(VIEW_NAME, view.tableName)

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
      val mapper: (String) -> String = { if (escapeDatabaseEntities) escapeDatabaseEntity(it) else it }
      val tableName = mapper.invoke(column.table.tableName)
      val columnName = mapper.invoke(column.columnName)

      autoincrementColumnStatement.replace(NEXT_VALUE, startValue.toString())
        .replace(STEP_VALUE, stepValue.toString())
        .replace(TABLE_NAME, tableName)
        .replace(COLUMN_NAME, columnName)
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
      H2DB, HSQLDB, POSTGRESQL, MARIADB -> "CASCADE"
      ORACLE -> "CASCADE CONSTRAINTS"
      else -> ""
    }

  fun arrayType(typeName: String): String = when (this) {
    HSQLDB -> "$typeName ARRAY"
    POSTGRESQL -> "$typeName[]"
    MSSQL -> "ARRAY<$typeName>"
    ORACLE ->  // CREATE TYPE string_array AS VARRAY(NULL ) OF VARCHAR(50);
      when {
        typeName.contains("CHAR") || typeName.contains("TEXT") || typeName.contains("CLOB") -> "SYS.ODCIVARCHAR2LIST"
        typeName.contains("INT") || typeName.contains("NUMBER") -> "SYS.ODCINUMBERLIST"
        typeName == "DATE" -> "SYS.ODCIDATELIST"
        else -> " VARRAY(32768) OF $typeName" // Actually give up
      }

    else -> "$typeName[]"
  }

  /**
   * @return clause with template variable to be replaced with actual data
   */
  val blobDataClause: String
    get() = when (this) {
      POSTGRESQL -> """E'\\x$HEX_VALUE'"""
      SYBASE, MSSQL, MYSQL, MARIADB -> """0x$HEX_VALUE"""
      ORACLE -> """'$HEX_VALUE'"""
      IBMDB2 -> """BLOB(x'$HEX_VALUE')"""
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

  fun supportsPrecisionClause(type: String) =
    (type.contains("CHAR") && !type.contains("LONG"))
      || type == DECIMAL.name || type == NUMERIC.name
      || type == BINARY.name || type == VARBINARY.name
      || (ORACLE == this && (type == "VARCHAR2" || type == "NUMBER"))
      || (IBMDB2 == this && (type == "VARGRAPHIC"))

  private fun retrieveAutoIncrementValue(column: ColumnMetaData): AutoIncrementValue {
    val connectorRepository = column.connectorRepository
    val connectorId = column.connectorId

    return connectorRepository.hint<AutoIncrementValue>(connectorId)
  }
}