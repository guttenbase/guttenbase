@file:Suppress("unused")

package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.databaseType
import java.sql.JDBCType.*

/**
 * Create (initial) column type definition for given column and database type.
 * Handle proprietary column types here by adding custom resolvers.
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
object ProprietaryColumnTypeDefinitionFactory : ColumnTypeDefinitionFactory {
  private val resolvers = mutableListOf<ColumnTypeDefinitionFactory>(
    ColumnTypeDefinitionFactory { sourceColumn, targetDatabase ->
      when (sourceColumn.databaseType) {
        ORACLE -> if (sourceColumn.columnTypeName == "NUMBER" && sourceColumn.scale == 0) {
          ColumnTypeDefinition(sourceColumn, targetDatabase, "BIGINT", BIGINT)
        } else null

        MYSQL -> if (sourceColumn.columnTypeName == "GEOMETRY") {
          ColumnTypeDefinition(sourceColumn, targetDatabase, "BLOB", BLOB)
        } else null

//        MSSQL -> if (sourceColumn.columnTypeName == "MONEY") {
//          ColumnTypeDefinition(sourceColumn, targetDatabase, "DECIMAL", DECIMAL)
//        } else null

        // https://www.ibm.com/docs/en/iis/11.5?topic=dts-db2-data-type-support
        IBMDB2 -> if (sourceColumn.jdbcColumnType == VARCHAR && sourceColumn.columnTypeName == "VARGRAPHIC") {
          ColumnTypeDefinition(sourceColumn, targetDatabase, "NVARCHAR", NVARCHAR)
        } else if (sourceColumn.jdbcColumnType == CHAR && sourceColumn.columnTypeName == "GRAPHIC") {
          ColumnTypeDefinition(sourceColumn, targetDatabase, "NCHAR", NCHAR)
        } else null

        H2DB -> if (targetDatabase.databaseType == H2DB && sourceColumn.columnTypeName == "CHAR") {
          ColumnTypeDefinition(sourceColumn, targetDatabase, "CHARACTER", CHAR)
        } else null

        else -> null
      }
    },

    ColumnTypeDefinitionFactory { sourceColumn, targetDatabase ->
      if (sourceColumn.jdbcColumnType == BINARY && sourceColumn.precision == 1)
        ColumnTypeDefinition(sourceColumn, targetDatabase, "BIT", BIT) // Better fit
      else null
    },

    // Finally...
    ColumnTypeDefinitionFactory { sourceColumn, targetDatabase ->
      ColumnTypeDefinition(sourceColumn, targetDatabase, sourceColumn.columnTypeName)
    }
  )

  override fun createColumnDefinition(sourceColumn: ColumnMetaData, targetDatabase: DatabaseMetaData): ColumnTypeDefinition =
    resolvers.asSequence().map { it.createColumnDefinition(sourceColumn, targetDatabase) }.firstOrNull { it != null }
      ?: throw IllegalStateException("No column definition created for $sourceColumn")

  /**
   * Add custom resolver which is preferred over existing resolvers, i.e. it will be called first
   */
  fun addColumnTypeDefinitionFactory(resolver: ColumnTypeDefinitionFactory) {
    resolvers.add(0, resolver)
  }
}