package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData

/**
 * How many rows of tables shall be regarded when checking that data has been transferred correctly.
 *
 * What algorithm shall be used when checking a table:
 *
 * a) Via SELECT with order by and equality check column by column
 * b) Read all table data and compare the resulting list, especially useful where no primary key exists or no valid ORDER BY may bne generated
 *
 * &copy; 2012-2020 tech@spree
 *
 * @author M. Dahm
 */
interface CheckTableData {
	val numberOfCheckedRows: Int

	fun checkBy(targetTable: TableMetaData): ComparisonKind

	enum class ComparisonKind {
		SELECT, DATA_COMPARISON
	}
}
