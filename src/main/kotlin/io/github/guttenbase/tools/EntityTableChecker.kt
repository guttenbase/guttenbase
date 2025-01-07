package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData


/**
 * Check if the given table is a "main" table in the sense that it represents an entity. In terms of JPA: the corresponding Java class is
 * annotated with @Entity.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface EntityTableChecker {
  /**
   * @return true if the given table is a "main" table in the sense that it represents an entity. In terms of JPA: the corresponding Java
   * class is annotated with @Entity.
   */
  fun isEntityTable(tableMetaData: TableMetaData): Boolean
}
