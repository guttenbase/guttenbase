package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ForeignKeyMapper

/**
 * Map index name
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.schema.SchemaScriptCreatorTool] to map index names
 *
 * @author M. Dahm
 */
abstract class ForeignKeyMapperHint : ConnectorHint<ForeignKeyMapper> {
  override val connectorHintType: Class<ForeignKeyMapper>
    get() = ForeignKeyMapper::class.java
}
