package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import java.math.BigDecimal

/**
 * Map Long/Bigint to BigDecimal
 *
 *  &copy; 2012 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("unused")
class BigIntLongToBigDecimalColumnDataMapper : ColumnDataMapper {
  override fun map(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData, value: Any?): Any? {
    return if (value == null) null else BigDecimal(value.toString())
  }
}