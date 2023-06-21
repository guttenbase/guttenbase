package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryTableFilter

/**
 * Created by mfehler on 26.04.17.
 */
class TestTableNameFilterHint : RepositoryTableFilterHint() {
  override val value: RepositoryTableFilter
    get() = RepositoryTableFilter {
      val tableCase = it.tableName

      (tableCase.startsWith("TAB") || tableCase.startsWith("P") || tableCase.startsWith("O") || tableCase.startsWith("C")
          || tableCase.startsWith("FOO") || tableCase.startsWith("E"))
    }
}
