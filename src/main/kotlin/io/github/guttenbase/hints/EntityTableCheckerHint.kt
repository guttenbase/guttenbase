package io.github.guttenbase.hints

import io.github.guttenbase.tools.EntityTableChecker

/**
 * Check if the given table is a "main" table in the sense that it represents an entity. In terms of JPA: the corresponding Java class is
 * annotated with @Entity.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [AbstractSequenceUpdateTool] to look for entity classes, i.e. classes that may use an ID sequence
 *
 * @author M. Dahm
 */
abstract class EntityTableCheckerHint : ConnectorHint<EntityTableChecker> {
  override val connectorHintType: Class<EntityTableChecker>
    get() = EntityTableChecker::class.java
}
