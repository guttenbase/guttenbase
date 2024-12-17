# Tools

[Currently implemented in guttenbase](https://javadoc.io/doc/io.github.guttenbase/guttenbase/latest/io/github/guttenbase/guttenbase/tools/package-summary.html)
(among database-specific tools) are the following standard tools:

* DefaultTableCopyTool: Copy tables using the default algorithm. Basically read data in chunks from source, transform them and write batches of INSERT statements to the target data base. The number of rows and data items per batch/commit is configurable.
* ScriptExecutorTool: Execute SQL statements in various ways
* SchemaComparatorTool: Check for compatibility between source and target data base. I.e., check for tables, columns and if the column types can be mapped.
* CheckEqualTableDataTool: Check two schemas for equal data where the tool takes a configurable number of sample data from each table.
* ReadTableDataTool: Read data from table(s) and return them in a map
* SplitByRangeTableCopyTool: Same functionality, but splits the data by some given range, usually the primary key. I.e., the data is read in chunks where those chunks are split using the ID column range of values.
* AbstractSequenceUpdateTool: Some data bases support sequences. After migration the sequences must be updated to a new current value