package io.github.guttenbase.export

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository


/**
 * Import schema information and table data from some custom format.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface Importer {
  @Throws(Exception::class)
  fun initializeImport(
    connectorRepository: ConnectorRepository,
    connectorId: String,
    importDumpConnectionInfo: ImportDumpConnectionInfo
  )

  @Throws(Exception::class)
  fun finishImport()

  @Throws(Exception::class)
  fun readDatabaseMetaData(): DatabaseMetaData

  @Throws(Exception::class)
  fun readObject(): Any?

  @Throws(Exception::class)
  fun seekTableHeader(tableMetaData: TableMetaData)
}