package io.github.guttenbase.export.zip

import io.github.guttenbase.export.ImporterFactory
import io.github.guttenbase.hints.ConnectorHint


/**
 * Create @see [io.github.guttenbase.export.Importer] for reading dumped database using @see [io.github.guttenbase.export.ImportDumpConnector].
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.export.ImportDumpConnector] to determine importer implementation
 *
 * @author M. Dahm
 */
abstract class ImporterFactoryHint : ConnectorHint<ImporterFactory> {
  override val connectorHintType: Class<ImporterFactory>
    get() = ImporterFactory::class.java
}
