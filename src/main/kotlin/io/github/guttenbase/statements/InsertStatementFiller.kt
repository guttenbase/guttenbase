package io.github.guttenbase.statements

import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.MissingDataException
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.ColumnOrderHint.Companion.getSortedColumns
import io.github.guttenbase.hints.impl.DefaultColumnDataMapperProviderHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableRowDataFilter
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.CommonColumnTypeResolverTool
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Fill previously created INSERT statement with data from source connector.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [ColumnOrderHint] to determine column order
 */
class InsertStatementFiller(private val connectorRepository: ConnectorRepository) {
  private val closeables = ArrayList<Closeable>()

  @Throws(SQLException::class)
  fun fillInsertStatementFromResultSet(
    sourceConnectorId: String, sourceTableMetaData: TableMetaData,
    targetConnectorId: String, targetTableMetaData: TableMetaData,
    targetDatabaseConfiguration: TargetDatabaseConfiguration, targetConnection: Connection, rs: ResultSet,
    insertStatement: PreparedStatement, numberOfRowsPerBatch: Int, useMultipleValuesClauses: Boolean
  ) {
    val commonColumnTypeResolver = CommonColumnTypeResolverTool(connectorRepository)
    val sourceColumns = getSortedColumns(connectorRepository, sourceConnectorId, sourceTableMetaData)
    val columnMapper = connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java).value
    val filter = connectorRepository.getConnectorHint(targetConnectorId, TableRowDataFilter::class.java).value
    val targetDatabaseType = targetTableMetaData.databaseMetaData.databaseType
    var targetColumnIndex = 1
    var dataItemsCount = 0

    for (currentRow in 0 until numberOfRowsPerBatch) {
      val ok = rs.next()
      val sourceValues = LinkedHashMap<ColumnMetaData, Any?>()
      val targetValues = LinkedHashMap<ColumnMetaData, Any?>()
      var insertData = true

      if (!ok) {
        throw MissingDataException(
          "No more data in row " + currentRow + "/" + numberOfRowsPerBatch + " in " + sourceTableMetaData.tableName
        )
      }

      targetDatabaseConfiguration.beforeNewRow(targetConnection, targetConnectorId, targetTableMetaData)

      for (columnIndex in 1..sourceColumns.size) {
        val sourceColumnMetaData = sourceColumns[columnIndex - 1]
        val mapping = columnMapper.map(sourceColumnMetaData, targetTableMetaData)

        if (mapping.columns.isEmpty()) {
          if (mapping.isEmptyColumnListOk) {
            LOG.warn("Dropping column $sourceColumnMetaData")
            // Unused result, but we may have to skip the next data item from an underlying stream implementation
            rs.getObject(columnIndex)

            if (!sourceColumnMetaData.isNullable) {
              LOG.warn(
                """
                $sourceColumnMetaData does not allow null values, but value is ignored for target table.
                Make sure that the column is omitted during target table creation, too, or has a default value set.
                """.trimIndent()
              )
            }
          } else {
            throw IncompatibleColumnsException(
              "Cannot map column $targetTableMetaData:$sourceColumnMetaData: Target column list empty"
            )
          }
        }

        for (targetColumnMetaData in mapping.columns) {
          val columnTypeMapping = findMapping(
            targetConnectorId, commonColumnTypeResolver, sourceColumnMetaData, targetColumnMetaData
          )
          val sourceValue = columnTypeMapping.sourceColumnType.getValue(rs, columnIndex)
          val targetValue = columnTypeMapping.columnDataMapper.map(sourceColumnMetaData, targetColumnMetaData, sourceValue)
          val optionalCloseableObject = columnTypeMapping.targetColumnType.setValue(
            insertStatement, targetColumnIndex++, targetValue, targetDatabaseType,
            targetColumnMetaData.columnType
          )

          sourceValues[sourceColumnMetaData] = sourceValue
          targetValues[targetColumnMetaData] = targetValue

          if (optionalCloseableObject != null) {
            closeables.add(optionalCloseableObject)
          }

          dataItemsCount++
        }
      }

      // reset insert statement
      if (!filter.accept(sourceValues, targetValues)) {
        if (useMultipleValuesClauses) {
          throw SQLException(
            """${TableRowDataFilter::class.java.name} hint must not be used, if NumberOfRowsPerBatch hint allows multiple VALUES(...) clauses.
              Please disable it for table $targetTableMetaData""".trimIndent()
          )
        }

        insertData = false
      }

      // Add another INSERT with one VALUES clause to BATCH
      if (!useMultipleValuesClauses) {
        if (insertData) {
          insertStatement.addBatch()
        }

        targetColumnIndex = 1
      }

      targetDatabaseConfiguration.afterNewRow(targetConnection, targetConnectorId, targetTableMetaData)
    }

    // Add single INSERT with many VALUES clauses to BATCH
    if (useMultipleValuesClauses) {
      insertStatement.addBatch()
    }

    LOG.debug("Number of data items: $dataItemsCount")
  }

  private fun findMapping(
    targetConnectorId: String,
    commonColumnTypeResolver: CommonColumnTypeResolverTool,
    sourceColumn: ColumnMetaData,
    targetColumn: ColumnMetaData
  ) = commonColumnTypeResolver.getCommonColumnTypeMapping(sourceColumn, targetConnectorId, targetColumn)
    ?: throw IncompatibleColumnsException(
      """Columns have incompatible types: ${sourceColumn.columnName}/${sourceColumn.columnTypeName} vs. ${targetColumn.columnName}/${targetColumn.columnTypeName}
         Please add a mapping using ${DefaultColumnDataMapperProviderHint::class.java.name}, e.g.,
         connectorRepository.addConnectorHint(TARGET, new DefaultColumnDataMapperProviderHint() {
         @Override
         protected void addMappings(final DefaultColumnDataMapperProvider columnDataMapperFactory) {
           super.addMappings(columnDataMapperFactory);

           columnDataMapperFactory.addMapping(ColumnType.CLASS_STRING, ColumnType.CLASS_STRING, new ColumnDataMapper() {
             @Override
             public Object map(final ColumnMetaData sourceColumnMetaData, final ColumnMetaData targetColumnMetaData, final Object value) {
               return ...
             }
           });
         }
       });
      """.trimIndent()
    )

  /**
   * Clear any resources associated with this commit, open BLOBs in particular.
   */
  fun clear() {
    for (closeableObject in closeables) {
      try {
        closeableObject.close()
      } catch (e: IOException) {
        LOG.warn("While closing $closeableObject", e)
      }
    }
    closeables.clear()
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(InsertStatementFiller::class.java)
  }
}
