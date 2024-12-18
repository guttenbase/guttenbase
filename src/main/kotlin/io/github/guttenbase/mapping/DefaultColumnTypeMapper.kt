package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseColumnType
import io.github.guttenbase.meta.DatabaseMetaData
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
  private val DEFAULT_RESOLVER: ColumnDefinitionResolver = object : ColumnDefinitionResolver {
    override fun resolve(
      sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
    ): ColumnTypeDefinition? {
      val type = targetDatabase.typeFor(column)

      return if (type != null) {
        val precision = computePrecision(column, type)

        ColumnTypeDefinition(column, type.typeName, precision, column.scale)
      } else {
        null
      }
    }
  }

  private val resolvers = mutableListOf<ColumnDefinitionResolver>(
    DEFAULT_RESOLVER, ProprietaryColumnDefinitionResolver,
    // Finally return something equivalent
    ColumnDefinitionResolver { _, _, column ->
      ColumnTypeDefinition(column, column.columnTypeName, column.precision, column.scale)
    }
  )

  override fun lookupColumnDefinition(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ) = resolvers.asSequence()
    .map { it.resolve(sourceDatabase, targetDatabase, column) }
    .firstOrNull { it != null }
    ?: throw IllegalStateException("No column definition found for $column")

  /**
   * Add custom resolver which is preferred over existing resolvers
   */
  fun addColumnDefinitionResolver(resolver: ColumnDefinitionResolver) {
    resolvers.add(0, resolver)
  }
}

private val LOG = LoggerFactory.getLogger(DefaultColumnTypeMapper::class.java)

internal fun computePrecision(column: ColumnMetaData, type: DatabaseColumnType) =
  if (column.precision > type.estimatedEffectiveMaxPrecision) {
    LOG.warn(
      """Requested column precision of ${column.precision} for type ${column.jdbcColumnType} 
      is higher than ${type.estimatedEffectiveMaxPrecision} supported by $type""".trimIndent()
    )
    type.estimatedEffectiveMaxPrecision
  } else {
    column.precision
  }