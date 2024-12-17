package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import org.slf4j.LoggerFactory

fun interface ColumnDefinitionResolver {
  fun resolve(
    sourceDatabase: DatabaseMetaData,
    targetDatabase: DatabaseMetaData,
    column: ColumnMetaData
  ): ColumnDefinition?
}

/**
 * By default, try to map via supported types in DB. You may, however, add "overrides" that return a
 * custom [ColumnDefinition] in special cases that have to be resolved manually.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object DefaultColumnTypeMapper : AbstractColumnTypeMapper() {
  @JvmStatic
  private val LOG = LoggerFactory.getLogger(DefaultColumnTypeMapper::class.java)

  private val DEFAULT_RESOLVER: ColumnDefinitionResolver = object : ColumnDefinitionResolver {
    override fun resolve(
      sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
    ): ColumnDefinition? {
      val type = targetDatabase.typeFor(column)

      return if (type != null) {
        val precision = if (column.precision > type.precision) {
          LOG.warn("Requested column precision of ${column.precision} for type ${column.jdbcColumnType} is higher than supported by $type")
          type.precision
        } else {
          column.precision
        }

        ColumnDefinition(column, type.typeName, precision, column.scale, type.mayUsePrecision)
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
    .filter { it != null }
    .firstOrNull()

  /**
   * Add custom resolver which is preferred over existing resolvers
   */
  fun addColumnDefinitionResolver(resolver: ColumnDefinitionResolver) {
    resolvers.add(0, resolver)
  }
}