package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.EntityTableCheckerHint
import io.github.guttenbase.tools.EntityTableChecker

/**
 * By default we check if the given table has an primary key column named "ID".
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultEntityTableCheckerHint : EntityTableCheckerHint() {
  override val value: EntityTableChecker
    get() = EntityTableChecker { tableMetaData ->
      for (columnMetaData in tableMetaData.columnMetaData) {
        val columnName: String = columnMetaData.columnName.uppercase()

        if (columnMetaData.isPrimaryKey && (columnName == "ID" || columnName == "IDENT")) {
          return@EntityTableChecker true
        }
      }

      false
    }
}
