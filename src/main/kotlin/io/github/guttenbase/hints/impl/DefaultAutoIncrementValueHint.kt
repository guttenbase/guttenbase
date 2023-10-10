package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.AutoIncrementValueHint
import io.github.guttenbase.schema.AutoIncrementValue
import io.github.guttenbase.schema.DefaultAutoIncrementValue


/**
 * Determine start value and step factor for autoincrement (aka IDENTITY) columns
 *
 * Using this hint the @see [io.github.guttenbase.mapping.DefaultColumnTypeMapper] generate a clause with the given start value and step factor.
 */
open class DefaultAutoIncrementValueHint: AutoIncrementValueHint() {
  override val value: AutoIncrementValue
    get() = DefaultAutoIncrementValue()
}