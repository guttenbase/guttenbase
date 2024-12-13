package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.tools.ColumnMapping
import java.sql.SQLException

/**
 * Map data contained in a column to some other type. I.e., the target column may have a different type and the
 * INSERT statement arguments thus require some conversion.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ColumnDataMapper {
  /**
   * Mapper can be used for the given columns?
   */
  fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData): Boolean = true

  /**
   * Map object. Must be able to handle NULL values.
   */
  @Throws(SQLException::class)
  fun map(mapping: ColumnMapping, value: Any?): Any?
}
