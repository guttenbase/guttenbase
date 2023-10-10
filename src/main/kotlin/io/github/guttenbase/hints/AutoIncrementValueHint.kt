package io.github.guttenbase.hints

import io.github.guttenbase.schema.AutoIncrementValue

/**
 * Determine start value and step factor for autoincrement (aka IDENTITY) columns
 *
 * Using this hint the @see [io.github.guttenbase.mapping.DefaultColumnTypeMapper] generate a clause with the given start value and step factor.
 */
abstract class AutoIncrementValueHint : ConnectorHint<AutoIncrementValue> {
  override val connectorHintType: Class<AutoIncrementValue>
    get() = AutoIncrementValue::class.java
}
