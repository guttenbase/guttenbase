package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.SOURCE
import io.github.guttenbase.mapping.ColumnTypeDefinition
import io.github.guttenbase.mapping.DefaultColumnTypeMapper
import io.github.guttenbase.schema.SchemaScriptCreatorTool
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType.LONGVARCHAR

/**
 * Use custom column type resolvers
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class ColumnTypeDefinitionResolverTest : AbstractGuttenBaseTest() {

	@BeforeEach
	fun setupTables() {
		connectorRepository.addConnectionInfo(SOURCE, TestH2ConnectionInfo())
			.addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())

		scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-h2.sql")
		scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/data/test-data.sql")
	}

	@Test
	fun `Add custom column type resolver`() {
		DefaultColumnTypeMapper.addColumnTypeDefinitionResolver {
			if (it.sourceColumn.columnName == "NAME" && it.sourceColumn.table.tableName == "FOO_COMPANY") {
				ColumnTypeDefinition(it, "LONG VARCHAR", LONGVARCHAR)
			} else {
				null
			}
		}

		val schemaScriptCreatorTool = SchemaScriptCreatorTool(connectorRepository, SOURCE, DERBYDB)
		val tableStatements = schemaScriptCreatorTool.createTableStatements()
		scriptExecutorTool.executeScript(DERBYDB, true, true, tableStatements)

		val databaseMetaData = connectorRepository.getDatabase(DERBYDB)
		val tableMetaData = databaseMetaData.getTable("FOO_COMPANY")!!
		val column = tableMetaData.getColumn("NAME")!!

		assertThat(column.columnTypeName).isEqualTo("LONG VARCHAR")
	}
}
