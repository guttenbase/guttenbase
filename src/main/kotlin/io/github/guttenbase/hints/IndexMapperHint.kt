package io.github.guttenbase.hints

import io.github.guttenbase.mapping.IndexMapper

/**
 * Map index name
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.schema.SchemaScriptCreatorTool] to map index names
 *
 * @author M. Dahm
 */
abstract class IndexMapperHint : ConnectorHint<IndexMapper> {
  override val connectorHintType: Class<IndexMapper>
    get() = IndexMapper::class.java
}
