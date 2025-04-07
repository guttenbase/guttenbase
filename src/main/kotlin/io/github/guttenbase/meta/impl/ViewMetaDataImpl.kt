package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalViewMetaData
import io.github.guttenbase.meta.ViewMetaData
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

/**
 * Information about a view.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Serializable
class ViewMetaDataImpl(
  @Transient
  override var database: DatabaseMetaData = DB_FOR_SERIALIZATION,
  override val tableName: String,
  override val tableType: String,
  override val tableCatalog: String?,
  override val tableSchema: String?
) : InternalViewMetaData, DatabaseEntityMetaDataImpl() {
  constructor(database: DatabaseMetaData, table: ViewMetaData) : this(
    database, table.tableName, table.tableType, table.tableCatalog, table.tableSchema
  )

  override operator fun compareTo(other: ViewMetaData) =
    tableName.uppercase(Locale.getDefault()).compareTo(other.tableName.uppercase())
}
