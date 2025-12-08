package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ForeignKeyMetaData

/**
 * Map foreign key name, since naming conventions may be different in databases, hint is used by [io.github.guttenbase.schema.SchemaScriptCreatorTool]
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
fun interface ForeignKeyMapper {
  /**
   * Map index name
   */
  fun mapForeignKeyName(source: ForeignKeyMetaData): String
}
