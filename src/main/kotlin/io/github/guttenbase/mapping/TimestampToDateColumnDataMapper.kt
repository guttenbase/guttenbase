package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import java.sql.Date
import java.sql.Timestamp

/**
 * Map Timestamp to Date as some databases use a DATETIME column others a simple DATE.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TimestampToDateColumnDataMapper : ColumnDataMapper {
  override fun map(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData, value: Any?): Any? {
    return if (value == null) {
      null
    } else {
      Date((value as Timestamp).time)
    }
  }
}