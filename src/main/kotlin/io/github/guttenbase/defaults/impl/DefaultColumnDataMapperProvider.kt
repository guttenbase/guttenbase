package io.github.guttenbase.defaults.impl

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider.addMapping
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.mapping.ColumnTypeDefinition
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.*
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.utils.Util.toDate
import io.github.guttenbase.utils.Util.toLocalDate
import io.github.guttenbase.utils.Util.toLocalDateTime
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.sql.*
import java.sql.JDBCType.BIT

typealias CDM = Pair<DatabaseType?, ColumnDataMapper>

/**
 * Default implementation. To add or override mappings you may call [addMapping]
 *
 * &copy; 2012-2044 tech@spree
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
    addMapping(CLASS_DATE, CLASS_INTEGER, YearToIntColumnDataMapper)
    addMapping(CLASS_DATE, CLASS_BIGDECIMAL, YearToBigDecimalColumnDataMapper)
    addMapping(CLASS_DATE, CLASS_SHORT, DateToShortColumnDataMapper)

    addMapping(CLASS_BYTE, CLASS_SHORT, ToShortColumnDataMapper)
    addMapping(CLASS_BYTE, CLASS_INTEGER, ToIntColumnDataMapper)
    addMapping(CLASS_BYTE, CLASS_LONG, ToLongColumnDataMapper)

    addMapping(CLASS_SHORT, CLASS_INTEGER, ToIntColumnDataMapper)
    addMapping(CLASS_SHORT, CLASS_LONG, ToLongColumnDataMapper)
    addMapping(CLASS_INTEGER, CLASS_LONG, ToLongColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_LONG, ToLongColumnDataMapper)
    addMapping(CLASS_BIGDECIMAL, CLASS_INTEGER, ToIntColumnDataMapper)
    addMapping(CLASS_LONG, CLASS_LONG, ToLongColumnDataMapper)

    addMapping(CLASS_BYTE, CLASS_BIGDECIMAL, IntToBigDecimalColumnDataMapper)
    addMapping(CLASS_LONG, CLASS_BIGDECIMAL, IntToBigDecimalColumnDataMapper)
    addMapping(CLASS_SHORT, CLASS_BIGDECIMAL, IntToBigDecimalColumnDataMapper)
    addMapping(CLASS_INTEGER, CLASS_BIGDECIMAL, IntToBigDecimalColumnDataMapper)
    addMapping(CLASS_DOUBLE, CLASS_BIGDECIMAL, DoubleToBigDecimalColumnDataMapper)

    addMapping(CLASS_BIGDECIMAL, CLASS_DOUBLE, BigDecimalToDoubleColumnDataMapper)
    addMapping(CLASS_OBJECT, CLASS_DOUBLE, BigDecimalToDoubleColumnDataMapper)

    addMapping(CLASS_BLOB, CLASS_BYTES, DefaultColumnDataMapper)
    addMapping(CLASS_BYTES, CLASS_BLOB, BytesToBlobDataMapper)
    addMapping(CLASS_CLOB, CLASS_STRING, ClobDataMapper)
    addMapping(CLASS_STRING, CLASS_CLOB, StringToClobDataMapper)

    addMapping(CLASS_BYTE, CLASS_BYTE, ToBitMapper)
    addMapping(CLASS_BOOLEAN, CLASS_BYTE, ToBitMapper)
    addMapping(CLASS_BYTE, CLASS_BOOLEAN, ToBooleanMapper)
  }

  /**
   * {@inheritDoc}
   */
  override fun findMapping(
		sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData,
		sourceColumnType: ColumnType, targetColumnType: ColumnType
  ): ColumnDataMapper? = findMappings(sourceColumnType, targetColumnType).filter {
    (it.first == null || it.first == targetColumnMetaData.databaseType) // Matching DB type
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

object ToIntColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Number) value.toInt() else value
}

object ToShortColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Number) value.toShort() else value
}

object IntToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Number) value.toLong().toBigDecimal() else value
}

object ToLongColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Number) value.toLong() else value
}

object BigDecimalToDoubleColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is BigDecimal) value.toDouble() else value
}

object TimestampToDateColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Timestamp) {
      Date(value.toLocalDateTime().toLocalDate().toDate().time)
    } else value
}

object TimeToDateColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Time) {
      Date(value.toLocalDateTime().toLocalDate().toDate().time)
    } else value
}

object DateToTimeColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is java.util.Date) Time(value.time) else value
}

object DateToTimestampColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is java.util.Date) Timestamp(value.time) else value
}

object YearToIntColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Date) {
      if (mapping.columnTypeDefinition.sourceColumn.columnTypeName == "YEAR")
        value.toDate().toLocalDate().year else value.time.toInt()
    } else value
}

object YearToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Date) {
      val result = YearToIntColumnDataMapper.map(mapping, value) as Int
      result.toBigDecimal()
    } else value
}

object DateToShortColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Date) {
      val result = YearToIntColumnDataMapper.map(mapping, value) as Int
      result.toShort()
    } else value
}

object TimeToTimestampColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Time) Timestamp(value.time) else value
}

object TimestampToTimeColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Timestamp) Time(value.time) else value
}

@Suppress("unused")
object BigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is BigDecimal)
      value.toDouble().toBigDecimal(mapping.columnTypeDefinition)
    else value
}

object DoubleToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is Double)
      value.toBigDecimal(mapping.columnTypeDefinition)
    else value
}

object BytesToBlobDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    // Use BLOB directly in order to avoid reading the complete data into memory
    if (value != null)
      GBBlob(value as ByteArray)
    else null
}

object ClobDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    value as? Clob ?: value
}

object ToBooleanMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (mapping.sourceColumn.jdbcColumnType == BIT) {
      when (value) {
        is ByteArray -> value[0] != BYTE_ZERO
        is Number -> value.toByte() != BYTE_ZERO
        else -> value
      }
    } else value
}

object ToBitMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    when (value) {
      is Boolean -> if (value) BYTE_ONE else BYTE_ZERO
      is Number -> value.toByte()
      else -> value
    }
}

object StringToClobDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (value is String)
      GBClob(value)
    else value
}

internal fun Double.toBigDecimal(mapping: ColumnTypeDefinition): BigDecimal =
  BigDecimal(this, MathContext(mapping.precision))
    // precision may be smaller and thus cause an java.lang.ArithmeticException: Rounding necessary otherwise
    .setScale(mapping.scale, RoundingMode.HALF_DOWN)

internal const val BYTE_ZERO = 0.toByte()
internal const val BYTE_ONE = 1.toByte()
