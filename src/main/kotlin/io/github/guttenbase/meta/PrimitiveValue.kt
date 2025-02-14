package io.github.guttenbase.meta

import kotlinx.serialization.Serializable

/**
 * Serializable classes for polymorphic values.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
sealed class PrimitiveValue<T> : java.io.Serializable {
  abstract val value: T
}

@Serializable
data class StringValue(override val value: String) : PrimitiveValue<String>()

@Serializable
data class LongValue(override val value: Long) : PrimitiveValue<Long>()

@Serializable
data class IntValue(override val value: Int) : PrimitiveValue<Int>()

@Serializable
data class BooleanValue(override val value: Boolean) : PrimitiveValue<Boolean>()

fun Any.toPrimitiveValue(): PrimitiveValue<*> = when (this) {
  is String -> StringValue(this)
  is Int -> IntValue(this)
  is Long -> LongValue(this)
  is Boolean -> BooleanValue(this)
  else -> throw UnsupportedOperationException("Unsupported primitive value type for $this")
}