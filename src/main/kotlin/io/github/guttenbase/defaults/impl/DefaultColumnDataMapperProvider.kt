package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider.addMapping
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnTypeDefinition
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.*
import io.github.guttenbase.tools.ColumnMapping
import io.github.guttenbase.utils.Util.toDate
import io.github.guttenbase.utils.Util.toLocalDateTime
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.sql.Blob
import java.sql.Clob
import java.sql.Date
import java.sql.Time
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
    addMapping(CLASS_TIMESTAMP, CLASS_TIME, TimestampToTimeColumnDataMapper)
    addMapping(CLASS_TIME, CLASS_TIMESTAMP, TimeToTimestampColumnDataMapper)
    addMapping(CLASS_TIME, CLASS_DATE, TimeToDateColumnDataMapper)
    addMapping(CLASS_DATE, CLASS_TIME, DateToTimeColumnDataMapper)
    addMapping(CLASS_DATE, CLASS_TIMESTAMP, DateToTimestampColumnDataMapper)

    addMapping(CLASS_LONG, CLASS_BIGDECIMAL, LongToBigDecimalColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_LONG, BigDecimalToLongColumnDataMapper)
    addMapping(CLASS_INTEGER, CLASS_BIGDECIMAL, IntToBigDecimalColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_BIGDECIMAL, BigDecimalColumnDataMapper)
    addMapping(CLASS_DOUBLE, CLASS_BIGDECIMAL, DoubleToBigDecimalColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_DOUBLE, BigDecimalToDoubleColumnDataMapper)
    addMapping(CLASS_OBJECT, CLASS_DOUBLE, BigDecimalToDoubleColumnDataMapper)
    addMapping(CLASS_SHORT, CLASS_BIGDECIMAL, ShortToBigDecimalColumnDataMapper)

    addMapping(CLASS_BLOB, CLASS_BYTES, BlobDataMapper)
    addMapping(CLASS_CLOB, CLASS_STRING, ClobDataMapper)
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

object ShortToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Short) BigDecimal(value.toInt()) else value
}

object BigDecimalToLongColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is BigDecimal) value.toLong() else value
}

object BigDecimalToDoubleColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is BigDecimal) value.toDouble() else value
}

object TimestampToDateColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Timestamp) {
      Date(value.toLocalDateTime().toLocalDate().toDate().time)
    } else value
}

object TimeToDateColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Time) {
      Date(value.toLocalDateTime().toLocalDate().toDate().time)
    } else value
}

object DateToTimeColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is java.util.Date) Time(value.time) else value
}

object DateToTimestampColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is java.util.Date) Timestamp(value.time) else value
}

object TimeToTimestampColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Time) Timestamp(value.time) else value
}

object TimestampToTimeColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Timestamp) Time(value.time) else value
}

object BigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is BigDecimal)
      value.toDouble().toBigDecimal(mapping.columnTypeDefinition)
    else value
}

object DoubleToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    if (value is Double)
      value.toBigDecimal(mapping.columnTypeDefinition)
    else value
}

object BlobDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    // Use BLOB directly in order to avoid reading the complete data into memory
    value as? Blob ?: value
}

object ClobDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) =
    value as? Clob ?: value
}

private fun Double.toBigDecimal(mapping: ColumnTypeDefinition): BigDecimal =
  BigDecimal(this, MathContext(mapping.precision))
    // precision may be smaller and thus cause an java.lang.ArithmeticException: Rounding necessary otherwise
    .setScale(mapping.scale, RoundingMode.HALF_DOWN)
