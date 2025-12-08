package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DroppingColumnMapper
import io.github.guttenbase.mapping.ColumnMapper
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where ID columns have been renamed. ID in table USER became USER_ID, ID in table COMPANY became
 * COMPANY_ID and so on.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class DroppingColumnMapperHintTest : AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-missingColumn.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(TARGET, object : ColumnMapperHint() {
      override val value: ColumnMapper
        get() = DroppingColumnMapper().addDroppedColumn("FOO_USER", "PERSONAL_NUMBER")
    })
  }
}
