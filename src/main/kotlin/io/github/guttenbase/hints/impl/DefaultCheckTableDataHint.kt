package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.CheckTableDataHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.CheckTableData
import io.github.guttenbase.tools.CheckTableData.ComparisonKind.DATA_COMPARISON
import io.github.guttenbase.tools.CheckTableData.ComparisonKind.SELECT
import org.slf4j.LoggerFactory

/**
 * Default number of checked rows is 100.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultCheckTableDataHint : CheckTableDataHint() {
	override val value: CheckTableData
		get() = object : CheckTableData {
			override val numberOfCheckedRows: Int = 100

			override fun checkBy(targetTable: TableMetaData) = if (targetTable.primaryKeyColumns.size != 1) {
				LOG.warn("No/too many primary key column found for $targetTable!")

				DATA_COMPARISON
			} else {
				SELECT
			}
		}

		@JvmStatic
		private val LOG = LoggerFactory.getLogger(DefaultCheckTableDataHint::class.java)
}
