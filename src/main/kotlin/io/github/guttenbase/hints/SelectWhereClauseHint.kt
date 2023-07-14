package io.github.guttenbase.hints

import io.github.guttenbase.tools.SelectWhereClause


/**
 * Optionally configure the SELECT statement created to read data from source tables with a WHERE clause.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 *
 *
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool]
 * Hint is used by [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool]
 */
abstract class SelectWhereClauseHint : ConnectorHint<SelectWhereClause> {
  override val connectorHintType: Class<SelectWhereClause>
    get() = SelectWhereClause::class.java
}
