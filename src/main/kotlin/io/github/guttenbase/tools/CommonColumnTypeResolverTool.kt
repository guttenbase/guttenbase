package io.github.guttenbase.tools

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapper
import io.github.guttenbase.mapping.*
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.repository.impl.ClassNameColumnTypeResolver
import io.github.guttenbase.repository.impl.HeuristicColumnTypeResolver

/**
 * Try to find common type mapping applicable for source and target columns.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.hints.ColumnDataMapperProviderHint] to determine mapping between different column types
 *
 * @author M. Dahm
 */
open class CommonColumnTypeResolverTool(private val connectorRepository: ConnectorRepository) {
  private val columnTypeResolvers = sequenceOf(HeuristicColumnTypeResolver, ClassNameColumnTypeResolver)

  /**
   * Returns column type applicable for source and target columns or null if none can be found.
   */
  fun getCommonColumnTypeMapping(
    sourceColumnMetaData: ColumnMetaData,
    targetConnectorId: String,
    targetColumnMetaData: ColumnMetaData
  ): ColumnTypeMapping? = columnTypeResolvers
    .map { findMapping(it, sourceColumnMetaData, targetColumnMetaData, targetConnectorId) }
    .firstOrNull()

  fun getColumnType(columnMetaData: ColumnMetaData): ColumnType = columnTypeResolvers
    .map { it.getColumnType(columnMetaData) }.firstOrNull { ColumnType.CLASS_UNKNOWN != it } ?: ColumnType.CLASS_UNKNOWN

  private fun findMapping(
    columnTypeResolver: ColumnTypeResolver, sourceColumnMetaData: ColumnMetaData,
    targetColumnMetaData: ColumnMetaData, targetConnectorId: String
  ): ColumnTypeMapping? {
    val sourceColumnType = columnTypeResolver.getColumnType(sourceColumnMetaData)
    val targetColumnType = columnTypeResolver.getColumnType(targetColumnMetaData)

    if (ColumnType.CLASS_UNKNOWN != sourceColumnType && ColumnType.CLASS_UNKNOWN != targetColumnType) {
      val columnDataMapperFactory = connectorRepository.hint<ColumnDataMapperProvider>(targetConnectorId)
      val columnDataMapper = columnDataMapperFactory.findMapping(
        sourceColumnMetaData, targetColumnMetaData,
        sourceColumnType, targetColumnType
      )

      if (columnDataMapper != null) {
        return ColumnTypeMapping(sourceColumnType, targetColumnType, columnDataMapper)
      } else if (sourceColumnType == targetColumnType) {
        return ColumnTypeMapping(sourceColumnType, targetColumnType, DefaultColumnDataMapper())
      }
    }

    return null
  }
}
