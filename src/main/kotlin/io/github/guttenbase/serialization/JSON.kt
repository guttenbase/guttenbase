package io.github.guttenbase.serialization

import io.github.guttenbase.meta.*
import io.github.guttenbase.meta.impl.ColumnMetaDataImpl
import io.github.guttenbase.meta.impl.DatabaseMetaDataImpl
import io.github.guttenbase.meta.impl.ForeignKeyMetaDataImpl
import io.github.guttenbase.meta.impl.IndexMetaDataImpl
import io.github.guttenbase.meta.impl.TableMetaDataImpl
import io.github.guttenbase.meta.impl.ViewMetaDataImpl
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

private val module = SerializersModule {
  polymorphic(DatabaseMetaData::class) { subclass(DatabaseMetaDataImpl::class, DatabaseMetaDataImpl.serializer()) }
  polymorphic(TableMetaData::class) { subclass(TableMetaDataImpl::class, TableMetaDataImpl.serializer()) }
  polymorphic(ViewMetaData::class) { subclass(ViewMetaDataImpl::class, ViewMetaDataImpl.serializer()) }
  polymorphic(ColumnMetaData::class) { subclass(ColumnMetaDataImpl::class, ColumnMetaDataImpl.serializer()) }
  polymorphic(ForeignKeyMetaData::class) { subclass(ForeignKeyMetaDataImpl::class, ForeignKeyMetaDataImpl.serializer()) }
  polymorphic(IndexMetaData::class) { subclass(IndexMetaDataImpl::class, IndexMetaDataImpl.serializer()) }

  polymorphic(PrimitiveValue::class) {
    subclass(IntValue::class, IntValue.serializer())
    subclass(StringValue::class, StringValue.serializer())
    subclass(LongValue::class, LongValue.serializer())
    subclass(BooleanValue::class, BooleanValue.serializer())
  }
}

val JSON = Json {
  prettyPrint = true

  serializersModule = module
}