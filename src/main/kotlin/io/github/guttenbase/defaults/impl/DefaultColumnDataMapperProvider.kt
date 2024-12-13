package io.github.guttenbase.defaults.impl

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider.addMapping
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.*
import io.github.guttenbase.tools.ColumnMapping
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.sql.Date
import java.sql.Timestamp

typealias CDM = Pair<DatabaseType?, ColumnDataMapper>

/**
 * Default implementation. To add or override mappings you may call [addMapping]
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnDataMapperProvider : ColumnDataMapperProvider {
  private val mappings = HashMap<String, MutableList<CDM>>()

  init {
    addMapping(CLASS_TIMESTAMP, CLASS_DATE, TimestampToDateColumnDataMapper)
    addMapping(CLASS_LONG, CLASS_BIGDECIMAL, LongToBigDecimalColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_LONG, BigDecimalToLongColumnDataMapper)
    addMapping(CLASS_INTEGER, CLASS_BIGDECIMAL, IntToBigDecimalColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_BIGDECIMAL, MSSQLBigDecimalColumnDataMapper)
  }

  /**
   * {@inheritDoc}
   */
  override fun findMapping(
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData,
    sourceColumnType: ColumnType, targetColumnType: ColumnType, databaseType: DatabaseType?
  ) = findMappings(sourceColumnType, targetColumnType).filter {
    (it.first == null || it.first == databaseType)
        && it.second.isApplicable(sourceColumnMetaData, targetColumnMetaData)
  }.map { it.second }.firstOrNull()

  /**
   * Add custom mapping for data types, optionally as DB-specific mapping only
   */
  @JvmOverloads
  fun addMapping(
    sourceColumnType: ColumnType, targetColumnType: ColumnType,
    columnDataMapper: ColumnDataMapper, databaseType: DatabaseType? = null
  ) {
    findMappings(sourceColumnType, targetColumnType).add(0, databaseType to columnDataMapper)
  }

  private fun createKey(sourceColumnType: ColumnType, targetColumnType: ColumnType) =
    sourceColumnType.name + ":" + targetColumnType.name

  private fun findMappings(sourceColumnType: ColumnType, targetColumnType: ColumnType): MutableList<CDM> {
    val key = createKey(sourceColumnType, targetColumnType)

    return mappings.getOrPut(key) { ArrayList() }
  }
}

object LongToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Long) BigDecimal(value) else value
}

object IntToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Int) BigDecimal(value) else value
}

object BigDecimalToLongColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is BigDecimal) value.toLong() else value
}

object TimestampToDateColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Timestamp) Date(value.time) else value
}

// MSSQL gets confused by non-scaled values
object MSSQLBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is BigDecimal)
      BigDecimal(value.toDouble(), MathContext(mapping.columnDefinition.precision))
        // precision may be smaller and thus cause an java.lang.ArithmeticException: Rounding necessary otherwise
        .setScale(mapping.columnDefinition.scale, RoundingMode.HALF_DOWN)
    else value
}