package io.github.guttenbase.tools

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.mapping.ColumnTypeMapper
import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
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
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData
  ): ColumnDataMapping?  = columnTypeResolvers.asSequence()
      .map { findColumnDataMapping(it, sourceColumnMetaData, targetColumnMetaData) }.firstOrNull()

  private fun findColumnDataMapping(
    resolver: ColumnTypeResolver, sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData
  ): ColumnDataMapping? {
    val sourceColumnType = resolver.getColumnType(sourceColumnMetaData)
    val targetColumnType = resolver.getColumnType(targetColumnMetaData)

    if (null != sourceColumnType && null != targetColumnType &&
      ColumnType.CLASS_UNKNOWN != sourceColumnType && ColumnType.CLASS_UNKNOWN != targetColumnType
    ) {
      val targetConnectorId = targetColumnMetaData.connectorId
      val columnDataMapperFactory = connectorRepository.hint<ColumnDataMapperProvider>(targetConnectorId)
      val columnTypeDefinition = connectorRepository.hint<ColumnTypeMapper>(targetConnectorId)
        .lookupColumnDefinition(sourceColumnMetaData, targetColumnMetaData.tableMetaData.databaseMetaData)
      val columnDataMapper = columnDataMapperFactory.findMapping(
        sourceColumnMetaData, targetColumnMetaData, sourceColumnType, targetColumnType
      )

      if (columnDataMapper != null) {
        return ColumnDataMapping(
          sourceColumnMetaData, targetColumnMetaData, sourceColumnType, targetColumnType, columnDataMapper,
          columnTypeDefinition
        )
      } else if (sourceColumnType == targetColumnType) {
        return ColumnDataMapping(
          sourceColumnMetaData, targetColumnMetaData, sourceColumnType, targetColumnType, DefaultColumnDataMapper,
          columnTypeDefinition
        )
      }
    }

    return null
  }
}
