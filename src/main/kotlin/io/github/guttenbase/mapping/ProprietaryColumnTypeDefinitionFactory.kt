@file:Suppress("unused")

package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData

/**
 * Create (initial) column type definition for given column and database type.
 * Handle proprietary column types here by adding custom resolvers.
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
object ProprietaryColumnTypeDefinitionFactory : ColumnTypeDefinitionFactory {
  private val resolvers = mutableListOf<ColumnTypeDefinitionFactory>(
    ColumnTypeDefinitionFactory { sourceColumn, targetDatabase ->
      ColumnTypeDefinition(sourceColumn, targetDatabase, sourceColumn.columnTypeName)
    }
  )

  override fun createColumnDefinition(sourceColumn: ColumnMetaData, targetDatabase: DatabaseMetaData): ColumnTypeDefinition =
    resolvers.asSequence().map { it.createColumnDefinition(sourceColumn, targetDatabase) }.first()

  /**
   * Add custom resolver which is preferred over existing resolvers, i.e. it will be called first
   */
  fun addColumnTypeDefinitionFactory(resolver: ColumnTypeDefinitionFactory) {
    resolvers.add(0, resolver)
  }
}