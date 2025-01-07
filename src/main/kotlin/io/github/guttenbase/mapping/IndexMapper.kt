package io.github.guttenbase.mapping

import io.github.guttenbase.meta.IndexMetaData

/**
 * Map index name, since naming conventions may be different in databases, hint is used by [io.github.guttenbase.schema.SchemaScriptCreatorTool]
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface IndexMapper {
  /**
   * Map index name
   */
  fun mapIndexName(source: IndexMetaData): String
}
