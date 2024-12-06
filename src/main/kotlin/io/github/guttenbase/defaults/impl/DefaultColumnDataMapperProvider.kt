package io.github.guttenbase.defaults.impl

import io.github.guttenbase.mapping.BigDecimalToLongColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.LongToBigDecimalColumnDataMapper
import io.github.guttenbase.mapping.TimestampToDateColumnDataMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType

/**
 * Default implementation. To add or override mappings you may call [addMapping]
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnDataMapperProvider : ColumnDataMapperProvider {
  private val mappings = HashMap<String, MutableList<ColumnDataMapper>>()

  init {
    addMapping(ColumnType.CLASS_TIMESTAMP, ColumnType.CLASS_DATE, TimestampToDateColumnDataMapper())
    addMapping(ColumnType.CLASS_TIMESTAMP, ColumnType.CLASS_DATETIME, TimestampToDateColumnDataMapper())
    addMapping(ColumnType.CLASS_LONG, ColumnType.CLASS_BIGDECIMAL, LongToBigDecimalColumnDataMapper())
    addMapping(ColumnType.CLASS_BIGDECIMAL, ColumnType.CLASS_LONG, BigDecimalToLongColumnDataMapper())
    addMapping(ColumnType.CLASS_INTEGER, ColumnType.CLASS_BIGDECIMAL, LongToBigDecimalColumnDataMapper())
  }

  /**
   * {@inheritDoc}
   */
  override fun findMapping(
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData,
    sourceColumnType: ColumnType, targetColumnType: ColumnType
  ) = findMapping(sourceColumnType, targetColumnType).firstOrNull {
    it.isApplicable(
      sourceColumnMetaData,
      targetColumnMetaData
    )
  }

  /**
   * {@inheritDoc}
   */
  fun addMapping(sourceColumnType: ColumnType, targetColumnType: ColumnType, columnDataMapper: ColumnDataMapper) {
    findMapping(sourceColumnType, targetColumnType).add(0, columnDataMapper)
  }

  private fun createKey(sourceColumnType: ColumnType, targetColumnType: ColumnType) =
    sourceColumnType.name + ":" + targetColumnType.name

  private fun findMapping(sourceColumnType: ColumnType, targetColumnType: ColumnType): MutableList<ColumnDataMapper> {
    val key = createKey(sourceColumnType, targetColumnType)

    return mappings.getOrPut(key) { ArrayList() }
  }
}
