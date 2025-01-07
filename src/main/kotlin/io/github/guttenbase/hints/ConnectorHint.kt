package io.github.guttenbase.hints

/**
 * Users may add configuration "hints" that influence the tools. E.g., the buffer size when reading or writing data. There is always a
 * default implementation added to a connector by the repository which may be overridden subsequently.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ConnectorHint<T> {
  val connectorHintType: Class<T>
  val value: T
}
