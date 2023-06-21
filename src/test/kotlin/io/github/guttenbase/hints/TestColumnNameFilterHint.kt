package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryColumnFilter

/**
 * Created by mfehler on 26.04.17.
 */
class TestColumnNameFilterHint : RepositoryColumnFilterHint() {
  override val value: RepositoryColumnFilter
    get() = RepositoryColumnFilter {
      val columnCase = it.columnName

      columnCase.startsWith("E") || columnCase.startsWith("C") || columnCase.startsWith("O")
          || columnCase.startsWith("SA") || columnCase.startsWith("R")
          || (columnCase.startsWith("H") || columnCase.startsWith("I")
          || columnCase.startsWith("P"))
    }
}
