package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultTableMapper
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.SchemaScriptCreatorToolTest
import io.github.guttenbase.tools.SchemaScriptCreatorToolTest.Companion.SCHEMA_NAME
import io.github.guttenbase.tools.SchemaScriptCreatorToolTest.Companion.TABLE
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DefaultTableMapperTest {
  @Test
  fun testDefaultNameMapper() {
    val databaseMetaData = SchemaScriptCreatorToolTest.createDatabaseMetaData(ConnectorRepository())
    val tableName = TABLE + 1
    val tableMetaData = databaseMetaData.getTable(tableName)!!
    assertEquals(tableName, DefaultTableMapper().mapTableName(tableMetaData, databaseMetaData))
    assertEquals("$SCHEMA_NAME.\"$tableName\"", DefaultTableMapper().fullyQualifiedTableName(tableMetaData, databaseMetaData))
    assertEquals(
      "$SCHEMA_NAME.\"${tableName.lowercase()}\"",
      DefaultTableMapper(CaseConversionMode.LOWER).fullyQualifiedTableName(tableMetaData, databaseMetaData)
    )
    assertEquals(
      "$SCHEMA_NAME.\"${tableName.uppercase()}\"",
      DefaultTableMapper(CaseConversionMode.UPPER).fullyQualifiedTableName(tableMetaData, databaseMetaData)
    )
  }
}
