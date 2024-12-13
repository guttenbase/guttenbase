package io.github.guttenbase.tools

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.mapping.ColumnDefinition
import io.github.guttenbase.mapping.ColumnTypeMapper
import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.repository.impl.ClassNameColumnTypeResolver
import io.github.guttenbase.repository.impl.HeuristicColumnTypeResolver
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
  private val columnTypeResolvers =
    mutableListOf(JDBCColumnTypeResolver, HeuristicColumnTypeResolver, ClassNameColumnTypeResolver)

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
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData
  ): ColumnMapping? {
    val dataMapping =
      columnTypeResolvers.map { it.findMapping(sourceColumnMetaData, targetColumnMetaData) }.firstOrNull()
    if (dataMapping != null) {
      val targetConnectorId = targetColumnMetaData.tableMetaData.databaseMetaData.connectorId

      val columnDefinition = connectorRepository.hint<ColumnTypeMapper>(targetConnectorId)
        .lookupColumnDefinition(
          sourceColumnMetaData,
          sourceColumnMetaData.tableMetaData.databaseMetaData, targetColumnMetaData.tableMetaData.databaseMetaData
        )

      return ColumnMapping(dataMapping, columnDefinition)
    } else {
      return null
    }
  }

  private fun ColumnTypeResolver.findMapping(
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData
  ): ColumnDataMapping? {
    val sourceColumnType = getColumnType(sourceColumnMetaData)
    val targetColumnType = getColumnType(targetColumnMetaData)
    val targetConnectorId = targetColumnMetaData.tableMetaData.databaseMetaData.connectorId

    if (ColumnType.CLASS_UNKNOWN != sourceColumnType && ColumnType.CLASS_UNKNOWN != targetColumnType) {
      val columnDataMapperFactory = connectorRepository.hint<ColumnDataMapperProvider>(targetConnectorId)
      val columnDataMapper = columnDataMapperFactory.findMapping(
        sourceColumnMetaData, targetColumnMetaData, sourceColumnType, targetColumnType,
        targetColumnMetaData.tableMetaData.databaseMetaData.databaseType
      )

      if (columnDataMapper != null) {
        return ColumnDataMapping(
          sourceColumnMetaData, targetColumnMetaData,
          sourceColumnType, targetColumnType,
          columnDataMapper
        )
      } else if (sourceColumnType == targetColumnType) {
        return ColumnDataMapping(
          sourceColumnMetaData, targetColumnMetaData,
          sourceColumnType, targetColumnType,
          DefaultColumnDataMapper()
        )
      }
    }

    return null
  }
}

data class ColumnMapping(val columnDataMapping: ColumnDataMapping, val columnDefinition: ColumnDefinition)
