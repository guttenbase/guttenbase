package io.github.guttenbase.schema

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import io.github.guttenbase.tools.ScriptExecutorTool.Companion.DEFAULT_EXCEPTIONHANDLER
import io.github.guttenbase.tools.ScriptExecutorTool.Companion.WARNING_EXCEPTIONHANDLER
import java.sql.SQLException
import kotlin.collections.flatten

/**
 * Create Custom DDL from existing schema
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 */
@Suppress("MemberVisibilityCanBePrivate")
class CopySchemaTool(
  private val connectorRepository: ConnectorRepository,
  private val sourceConnectorId: String, private val targetConnectorId: String
) {
  fun createDDLScript(): List<String> {
    return createDDLScripts().flatten()
  }

  fun createDDLScripts(): List<List<String>> {
    val schemaScriptCreatorTool = SchemaScriptCreatorTool(connectorRepository, sourceConnectorId, targetConnectorId)

    return listOf(
      schemaScriptCreatorTool.createTableStatements(),
      schemaScriptCreatorTool.createMultiColumnPrimaryKeyStatements(),
      schemaScriptCreatorTool.createForeignKeyStatements(),
      schemaScriptCreatorTool.createIndexStatements(),
      schemaScriptCreatorTool.createAutoincrementUpdateStatements()
    )
  }

  @Throws(SQLException::class)
  @JvmOverloads
  fun copySchema(strictErrorHandlingOnIndexes: Boolean = true) {
    val errorHandler = if (strictErrorHandlingOnIndexes) DEFAULT_EXCEPTIONHANDLER else WARNING_EXCEPTIONHANDLER
    val ddlScripts = createDDLScripts()

    val tool = ScriptExecutorTool(connectorRepository)

    tool.executeScript(targetConnectorId, lines = ddlScripts[0])
    tool.executeScript(targetConnectorId, lines = ddlScripts[1])
    tool.executeScript(targetConnectorId, lines = ddlScripts[2])
    // Indexes may fail on certain DBs and are less important
    tool.executeScript(targetConnectorId, lines = ddlScripts[3], errorHandler = errorHandler)
    tool.executeScript(targetConnectorId, lines = ddlScripts[4])
  }
}
