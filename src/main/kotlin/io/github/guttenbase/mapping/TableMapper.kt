package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * Select target table for given source table.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface TableMapper {
  /**
   * @return matching table in target data base or null
   */
  fun map(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): TableMetaData?

  /**
   * @return plain table name in target data base
   */
  fun mapTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): String

  /**
   * @return fully qualifed table name in target data base, i.e. including schema prefix and escape characters
   */
  fun fullyQualifiedTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): String
}
