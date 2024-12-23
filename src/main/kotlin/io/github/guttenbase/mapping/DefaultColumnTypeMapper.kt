package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseColumnType
import io.github.guttenbase.meta.DatabaseMetaData
import org.slf4j.LoggerFactory

/**
 * This class maintains a list of [ColumnTypeDefinitionResolver]s and asks thema one after another to
 * create a [ColumnTypeDefinition] for a given column and database type.
 *
 * Users may add custom [ColumnTypeDefinitionResolver]s in special cases that need to be resolved manually.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object DefaultColumnTypeMapper : AbstractColumnTypeMapper() {
  private val resolvers = mutableListOf<ColumnTypeDefinitionResolver>(
    ProprietaryColumnTypeDefinitionResolver, DatabaseColumnTypeDefinitionResolver,

    // Finally, we copy the original definition from the column as the last resort
    ColumnTypeDefinitionResolver { _, _, column ->
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
   * Add custom resolver which is preferred over existing resolvers, i.e. it will be called first
   */
  fun addColumnTypeDefinitionResolver(resolver: ColumnTypeDefinitionResolver) {
    resolvers.add(0, resolver)
  }
}

private val LOG = LoggerFactory.getLogger(DefaultColumnTypeMapper::class.java)

internal fun computePrecision(column: ColumnMetaData, type: DatabaseColumnType): Int =
  when {
    type.maxPrecision < 0 -> column.precision // Dunno, depends on target database

    column.precision > type.estimatedEffectiveMaxPrecision -> {
      LOG.debug(
        """
      Requested column precision of ${column.precision} for type ${column.jdbcColumnType} 
      is higher than ${type.estimatedEffectiveMaxPrecision} supported by $type
    """.trimIndent()
      )

      type.maxPrecision
    }

    else ->
      column.precision
  }

