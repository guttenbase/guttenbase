package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import java.math.BigDecimal

/**
 * Map BigDecimal to Long/Bigint
 *
 *
 *  &copy; 2013 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class BigDecimalToLongColumnDataMapper : ColumnDataMapper {
  override fun map(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData, value: Any?): Any? {
    return if (value != null) {
      val bigDecimal: BigDecimal = value as BigDecimal
      bigDecimal.toLong()
    } else {
      null
    }
  }
}
