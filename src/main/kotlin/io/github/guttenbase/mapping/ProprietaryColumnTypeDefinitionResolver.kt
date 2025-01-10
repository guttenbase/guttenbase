package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseType.*
import java.sql.JDBCType.*

/**
 * Try to resolve types using heuristic mappings of proprietary DB types
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
object ProprietaryColumnTypeDefinitionResolver : ColumnTypeDefinitionResolver {
  /**
   * Resolve column type definition for given column and database type by lookup in specified matrix of [ColumnTypeDefinitionResolver]s.
   */
  override fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition? =
    when (type.sourceDatabase.databaseType) {
      ORACLE -> if (type.jdbcType == TIMESTAMP && type.typeName == "DATE") {
        // For some reason, Oracle reports JDBC type TIMESTAMP for DATE columns??
        ColumnTypeDefinition(type, "DATE", DATE) // That type should be present in any DB!
      } else {
        null
      }

      else -> null
    } ?: when (type.targetDatabase.databaseType) {
      MSSQL -> when (type.jdbcType) {
        CLOB -> ColumnTypeDefinition(type, "VARCHAR(MAX)", LONGVARCHAR)
        LONGVARCHAR -> ColumnTypeDefinition(type, "VARCHAR(MAX)", LONGVARCHAR)
        NUMERIC, DECIMAL -> if (type.scale > 0) {
          // When using DECIMAL(8, 5), e.g. MSSQL is messing up values by rounding them to wrong values
          ColumnTypeDefinition(type, "FLOAT", DOUBLE) // Why so ever is a double called "FLOAT" by MSSQL? 🙄
        } else {
          ColumnTypeDefinition(type, "INT", INTEGER)
        }

        else -> null
      }

      MYSQL, MARIADB -> when (type.jdbcType) {
        VARCHAR -> if (type.precision > 16300) ColumnTypeDefinition(type, "TEXT", LONGVARCHAR) else null
        VARBINARY -> if (type.precision > 65000) ColumnTypeDefinition(type, "BLOB", BLOB) else null
        else -> null
      }

      ORACLE -> when (type.jdbcType) {
        DOUBLE -> ColumnTypeDefinition(type, "DOUBLE PRECISION", DOUBLE)
        else -> null
      }

      HSQLDB -> when (type.typeName) {
        "TEXT" -> ColumnTypeDefinition(type, "LONGVARCHAR")
        else -> null
      }

      else -> null
    }
}
