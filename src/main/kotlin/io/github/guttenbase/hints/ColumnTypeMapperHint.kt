package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnTypeMapper

abstract class ColumnTypeMapperHint : ConnectorHint<ColumnTypeMapper> {
  override val connectorHintType: Class<ColumnTypeMapper>
    get() = ColumnTypeMapper::class.java
}
