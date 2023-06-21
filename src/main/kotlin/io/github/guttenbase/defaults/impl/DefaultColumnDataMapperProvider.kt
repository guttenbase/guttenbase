package io.github.guttenbase.defaults.impl

import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType

/**
 * Default implementation. To add further mapping you should simply extend [io.github.guttenbase.hints.impl.DefaultColumnDataMapperProviderHint] and call
 * [.addMapping] in the overridden
 * [io.github.guttenbase.hints.impl.DefaultColumnDataMapperProviderHint.addMappings] method.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnDataMapperProvider : ColumnDataMapperProvider {
  private val mappings = HashMap<String, MutableList<ColumnDataMapper>>()

  /**
   * {@inheritDoc}
   */
  override fun findMapping(
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData,
    sourceColumnType: ColumnType, targetColumnType: ColumnType
  ) = findMapping(sourceColumnType, targetColumnType).firstOrNull { it.isApplicable(sourceColumnMetaData, targetColumnMetaData) }

  /**
   * {@inheritDoc}
   */
  override fun addMapping(sourceColumnType: ColumnType, targetColumnType: ColumnType, columnDataMapper: ColumnDataMapper) {
    findMapping(sourceColumnType, targetColumnType).add(columnDataMapper)
  }

  private fun createKey(sourceColumnType: ColumnType, targetColumnType: ColumnType) =
    sourceColumnType.name + ":" + targetColumnType.name

  private fun findMapping(sourceColumnType: ColumnType, targetColumnType: ColumnType): MutableList<ColumnDataMapper> {
    val key = createKey(sourceColumnType, targetColumnType)

    return mappings.getOrPut(key) { ArrayList() }
  }
}
