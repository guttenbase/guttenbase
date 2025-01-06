package io.github.guttenbase.tools

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.mapping.ColumnTypeMapper
import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.connectorId
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.repository.impl.ClassNameColumnTypeResolver
import io.github.guttenbase.repository.impl.JDBCColumnTypeResolver

/**
 * Try to find common type mapping applicable for source and target columns.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * Tool is used by [io.github.guttenbase.statements.InsertStatementFiller], et.al. in order to determine mappings between different columns
 *
 * @author M. Dahm
 */
open class ColumnDataMappingTool(private val connectorRepository: ConnectorRepository) {
  private val columnTypeResolvers = mutableListOf(JDBCColumnTypeResolver, ClassNameColumnTypeResolver)

  /**
   * Insert custom [ColumnTypeResolver] (will be preferred over existing resolvers)
   */
  @Suppress("unused")
  fun addColumnTypeResolver(columnTypeResolver: ColumnTypeResolver) {
    columnTypeResolvers.add(0, columnTypeResolver)
  }

  /**
   * Returns column type mapping applicable for source and target columns or null if none can be found.
   */
  fun getCommonColumnTypeMapping(
    sourceColumn: ColumnMetaData, targetColumn: ColumnMetaData
  ): ColumnDataMapping? = columnTypeResolvers.asSequence()
    .map { findColumnDataMapping(it, sourceColumn, targetColumn) }.firstOrNull()

  private fun findColumnDataMapping(
    resolver: ColumnTypeResolver, sourceColumn: ColumnMetaData, targetColumn: ColumnMetaData
  ): ColumnDataMapping? {
    val sourceColumnType = resolver.getColumnType(sourceColumn)
    val targetColumnType = resolver.getColumnType(targetColumn)

    if (null != sourceColumnType && null != targetColumnType) {
      val targetConnectorId = targetColumn.connectorId
      val columnDataMapperFactory = connectorRepository.hint<ColumnDataMapperProvider>(targetConnectorId)
      val columnTypeDefinition = connectorRepository.hint<ColumnTypeMapper>(targetConnectorId)
        .lookupColumnDefinition(sourceColumn, targetColumn.tableMetaData.databaseMetaData)
      val columnDataMapper = columnDataMapperFactory.findMapping(
        sourceColumn, targetColumn, sourceColumnType, targetColumnType
      )

      if (columnDataMapper != null) {
        return ColumnDataMapping(
          sourceColumn, targetColumn, sourceColumnType, targetColumnType, columnDataMapper,
          columnTypeDefinition
        )
      } else if (sourceColumnType == targetColumnType || sourceColumn.columnTypeName == targetColumn.columnTypeName) { // Hope for the best...
        return ColumnDataMapping(
          sourceColumn, targetColumn, sourceColumnType, sourceColumnType, DefaultColumnDataMapper,
          columnTypeDefinition
        )
      }
    }

    return null
  }
}
