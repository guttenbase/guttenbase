package io.github.guttenbase.connector

open class GuttenBaseException : RuntimeException {
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)

}
