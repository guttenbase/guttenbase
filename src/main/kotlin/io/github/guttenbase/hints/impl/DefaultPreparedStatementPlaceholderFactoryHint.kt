package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.PreparedStatementPlaceholderFactoryHint
import io.github.guttenbase.mapping.PreparedStatementPlaceholderFactory
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.databaseType
import java.sql.JDBCType

/**
 * By default return just plain '?', plus some special handling for PostgreSQL.
 *
 * &copy; 2025-2034 tech@spree
 *
 * @author M. Dahm
 */
object DefaultPreparedStatementPlaceholderFactoryHint : PreparedStatementPlaceholderFactoryHint() {
  override val value: PreparedStatementPlaceholderFactory
    get() = PreparedStatementPlaceholderFactory { column ->
      when (column.databaseType) {
        DatabaseType.POSTGRESQL -> if (column.columnTypeName == JDBCType.BIT.name)
          "CAST(? AS ${column.columnTypeName})"
        else "?"

        else -> "?"
      }
    }
}
