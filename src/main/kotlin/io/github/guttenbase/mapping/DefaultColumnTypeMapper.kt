package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseSupportedColumnType
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.isDateType
import org.slf4j.LoggerFactory

/**
 * This class maintains a list of [ColumnTypeDefinitionResolver]s and asks them one after another to
 * create a [ColumnTypeDefinition] for a given column and database type.
 *
 * Users may add custom [ColumnTypeDefinitionResolver]s in special cases that need to be resolved manually.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object DefaultColumnTypeMapper : AbstractColumnTypeMapper() {
  private val resolvers = mutableListOf<ColumnTypeDefinitionResolver>(
    // Pass 0: Use user-defined resolvers, if any, see below #addColumnTypeDefinitionResolver()

    // Pass 1: Use heuristic knowledge about the target database to find a matching column type
    // Databases typically "know" more types than the JDBC driver reports
    ProprietaryColumnTypeDefinitionResolver,

    // Pass 2: Try to find matching column type in target database, i.e. JDBC type and type name match exactly
    // Should always be true for types like VARCHAR, INTEGER, etc.
    LookupPreciseMatchResolver,

    // Pass 4: Check know alternatives for given column type
    AlternateTypeResolver,

    // Pass 5: Try to find the best matching supported column type in the target database
    DatabaseColumnTypeDefinitionResolver,

    // Pass 6: Finally, we copy the original definition from the column as the last resort
    ColumnTypeDefinitionResolver {
      ColumnTypeDefinition(it.sourceColumn, it.targetDatabase, it.sourceColumn.columnTypeName)
    }
  )

  override fun lookupColumnTypeDefinition(
    targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ) = resolvers.asSequence()
    .map { it.resolve(ColumnTypeDefinition(column, targetDatabase, column.columnTypeName)) }
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

private val DatabaseSupportedColumnType.estimatedEffectiveMaxPrecision: Int
  get() = if (!jdbcType.isDateType()) (maxPrecision * 0.95).toInt() else maxPrecision

internal fun computePrecision(precision: Int, type: DatabaseSupportedColumnType): Int =
  when {
    type.maxPrecision < 0 -> precision // Dunno, depends on target database

    precision > type.estimatedEffectiveMaxPrecision -> {
      LOG.debug(
        """
      Requested column precision of $precision for type ${type.jdbcType} 
      is higher than ${type.estimatedEffectiveMaxPrecision} supported by $type
    """.trimIndent()
      )

      type.maxPrecision
    }

    else -> precision
  }

