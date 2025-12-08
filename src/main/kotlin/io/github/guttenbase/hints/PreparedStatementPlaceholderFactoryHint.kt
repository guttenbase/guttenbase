package io.github.guttenbase.hints

import io.github.guttenbase.mapping.PreparedStatementPlaceholderFactory

/**
 * Create placeholder for given columnn. In 99,9% percent of the cases you will just return a plain '?'
 * But there are situation that require special handling, e.g. the PostgreSQL JDBC driver does not allow to set the
 * value of a BIT column directly. Instead you have to usee an explicit cast in your statement, like `CAST(? AS BIT)`.
 *
 * &copy; 2025-2044 tech@spree
 *
 * @author M. Dahm
 */
abstract class PreparedStatementPlaceholderFactoryHint : ConnectorHint<PreparedStatementPlaceholderFactory> {
  override val connectorHintType: Class<PreparedStatementPlaceholderFactory>
    get() = PreparedStatementPlaceholderFactory::class.java
}
