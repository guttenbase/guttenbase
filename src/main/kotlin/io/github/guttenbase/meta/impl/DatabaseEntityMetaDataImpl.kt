package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.serialization.UUIDSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Information about a table or view.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Serializable
@Polymorphic
abstract class DatabaseEntityMetaDataImpl() : DatabaseEntityMetaData {
  // Workaround for kotlinx serialzation: https://github.com/Kotlin/kotlinx.serialization/issues/599#issuecomment-672889458
  abstract override val tableName: String

  /**
   * {@inheritDoc}
   */
  @Serializable(with = UUIDSerializer::class)
  override val syntheticId = UUID.randomUUID()!!

  /**
   * {@inheritDoc}
   */
  override var totalRowCount = 0

  protected val columnMap = LinkedHashMap<String, ColumnMetaData>()

  //
  // Derived values
  //
  override val columns: List<ColumnMetaData> get() = ArrayList(columnMap.values)

  override val columnCount: Int get() = columns.size

  /**
   * {@inheritDoc}
   */
  override fun getColumn(columnName: String): ColumnMetaData? = columnMap[columnName.uppercase()]

  override fun toString() = tableName

  override fun hashCode() = tableName.uppercase().hashCode()

  override fun equals(other: Any?) = other?.javaClass == javaClass && tableName.equals(javaClass.cast(other).tableName, ignoreCase = true)
}
