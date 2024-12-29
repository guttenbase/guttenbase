package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData

/**
 * Resolve column type definition by using supported types of DB as declared in [java.sql.DatabaseMetaData#getTypeInfo]
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object DatabaseColumnTypeDefinitionResolver : ColumnTypeDefinitionResolver {
  override fun resolve(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnTypeDefinition? {
    val type = targetDatabase.typeFor(column)

    return if (type != null) {
      val precision = computePrecision(column, type)

      ColumnTypeDefinition(column, targetDatabase, type.typeName, precision, column.scale)
    } else {
      null
    }
  }
}