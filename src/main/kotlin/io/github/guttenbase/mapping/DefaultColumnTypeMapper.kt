package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.supportsPrecisionClause
import org.slf4j.LoggerFactory

fun interface ColumnDefinitionResolver {
  fun resolve(
    sourceDatabase: DatabaseMetaData,
    targetDatabase: DatabaseMetaData,
    column: ColumnMetaData
  ): ColumnTypeDefinition?
}

/**
 * By default, try to map via supported types in DB. You may, however, add "overrides" that return a
 * custom [ColumnTypeDefinition] in special cases that have to be resolved manually.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object DefaultColumnTypeMapper : AbstractColumnTypeMapper() {
  @JvmStatic
  private val LOG = LoggerFactory.getLogger(DefaultColumnTypeMapper::class.java)

  private val DEFAULT_RESOLVER: ColumnDefinitionResolver = object : ColumnDefinitionResolver {
    override fun resolve(
      sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
    ): ColumnTypeDefinition? {
      val type = targetDatabase.typeFor(column)

      return if (type != null) {
        val precision = if (column.precision > type.maxPrecision) {
          LOG.warn("Requested column precision of ${column.precision} for type ${column.jdbcColumnType} is higher than supported by $type")
          type.maxPrecision
        } else {
          column.precision
        }

        ColumnTypeDefinition(
          column, type.typeName, precision, column.scale, type.supportsPrecisionClause && precision > 0
        )
      } else {
        null
      }
    }
  }

  private val resolvers = mutableListOf<ColumnDefinitionResolver>(DEFAULT_RESOLVER, ProprietaryColumnDefinitionResolver)

  override fun lookupColumnDefinition(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ) = resolvers.asSequence()
    .map { it.resolve(sourceDatabase, targetDatabase, column) }
    .firstOrNull { it != null }
    ?: createDefaultColumnDefinition(column)

  /**
   * Add custom resolver which is preferred over existing resolvers
   */
  fun addColumnDefinitionResolver(resolver: ColumnDefinitionResolver) {
    resolvers.add(0, resolver)
  }

  private fun createDefaultColumnDefinition(column: ColumnMetaData) = ColumnTypeDefinition(
    column, column.columnTypeName, column.precision, column.scale,
    column.jdbcColumnType.supportsPrecisionClause && column.precision > 0
  )
}