package io.github.guttenbase.tools

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapper
import io.github.guttenbase.mapping.*
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.repository.ConnectorRepository

/**
 * Try to find common type mapping usable for both columns.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 *
 * Hint is used by [ColumnDataMapperProviderHint] to determine mapping between different column types
 * Hint is used by [ColumnTypeResolverListHint] to determine mapping strategies between different column types
 *
 * @author M. Dahm
 */
class CommonColumnTypeResolverTool(private val connectorRepository: ConnectorRepository) {
  /**
   * Returns column type usable for both columns or null if none can be found.
   */
  fun getCommonColumnTypeMapping(
    sourceColumnMetaData: ColumnMetaData,
    targetConnectorId: String,
    targetColumnMetaData: ColumnMetaData
  ): ColumnTypeMapping? {
    val columnTypeResolvers: List<ColumnTypeResolver> = connectorRepository
      .getConnectorHint(targetConnectorId, ColumnTypeResolverList::class.java).value.getColumnTypeResolvers()

    return columnTypeResolvers.asSequence().map { findMapping(it, sourceColumnMetaData, targetColumnMetaData, targetConnectorId) }
      .firstOrNull()
  }

  fun getColumnType(connectorId: String, columnMetaData: ColumnMetaData): ColumnType {
    val columnTypeResolvers: List<ColumnTypeResolver> =
      connectorRepository.getConnectorHint(connectorId, ColumnTypeResolverList::class.java).value.getColumnTypeResolvers()

    return columnTypeResolvers.map { it.getColumnType(columnMetaData) }.firstOrNull { ColumnType.CLASS_UNKNOWN != it }
      ?: ColumnType.CLASS_UNKNOWN
  }

  private fun findMapping(
    columnTypeResolver: ColumnTypeResolver, sourceColumnMetaData: ColumnMetaData,
    targetColumnMetaData: ColumnMetaData, targetConnectorId: String
  ): ColumnTypeMapping? {
    val sourceColumnType: ColumnType = columnTypeResolver.getColumnType(sourceColumnMetaData)
    val targetColumnType: ColumnType = columnTypeResolver.getColumnType(targetColumnMetaData)

    if (ColumnType.CLASS_UNKNOWN != sourceColumnType && ColumnType.CLASS_UNKNOWN != targetColumnType) {
      val columnDataMapperFactory = connectorRepository.getConnectorHint(
        targetConnectorId, ColumnDataMapperProvider::class.java
      ).value
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
