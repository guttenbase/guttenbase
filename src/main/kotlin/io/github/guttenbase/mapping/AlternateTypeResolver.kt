package io.github.guttenbase.mapping

import java.sql.JDBCType.BIGINT
import java.sql.JDBCType.BINARY
import java.sql.JDBCType.BIT
import java.sql.JDBCType.BLOB
import java.sql.JDBCType.BOOLEAN
import java.sql.JDBCType.CHAR
import java.sql.JDBCType.CLOB
import java.sql.JDBCType.DATE
import java.sql.JDBCType.DECIMAL
import java.sql.JDBCType.DOUBLE
import java.sql.JDBCType.INTEGER
import java.sql.JDBCType.LONGVARBINARY
import java.sql.JDBCType.LONGVARCHAR
import java.sql.JDBCType.NUMERIC
import java.sql.JDBCType.SMALLINT
import java.sql.JDBCType.TINYINT
import java.sql.JDBCType.VARBINARY
import java.sql.JDBCType.VARCHAR

/**
 * Try to resolve types by mapping types to alternate "standard" types and looking up those in the target database.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
object AlternateTypeResolver : ColumnTypeDefinitionResolver {
  private val mappings = HashMap<String, List<ColumnTypeDefinitionResolver>>()

  init {
    mappings["LONGTEXT"] = listOf(TEXT_RESOLVER, LONGVARCHAR_RESOLVER, CLOB_RESOLVER)
    mappings["MEDIUMTEXT"] = listOf(TEXT_RESOLVER, LONGVARCHAR_RESOLVER, CLOB_RESOLVER)
    mappings["TEXT"] = listOf(TEXT_RESOLVER, LONGVARCHAR_RESOLVER, CLOB_RESOLVER)
    mappings["CLOB"] = listOf(LONGTEXT_RESOLVER, TEXT_RESOLVER)

    mappings["CHARACTER"] = listOf(CHAR_RESOLVER)
    mappings["BPCHAR"] = listOf(CHAR_RESOLVER)
    mappings["UNIQUEIDENTIFIER"] = listOf(CHAR_RESOLVER)
    mappings["CHARACTER VARYING"] = listOf(VARCHAR_RESOLVER)
    mappings["UUID"] = listOf(VARCHAR_RESOLVER)
    mappings["NVARCHAR"] = listOf(VARCHAR_RESOLVER)
    mappings["VARCHAR2"] = listOf(VARCHAR_RESOLVER)

    mappings["VARBINARY"] = listOf(BLOB_RESOLVER, IMAGE_RESOLVER, BYTEA_RESOLVER, BINARY_RESOLVER)
    mappings["BLOB"] = listOf(
      BLOB_RESOLVER, LONGVARBINARY_RESOLVER, VARBINARY_RESOLVER, VARBINARY_BIT_DATA_RESOLVER,
      BYTEA_RESOLVER, IMAGE_RESOLVER, BINARY_RESOLVER
    )
    mappings["BYTEA"] = listOf(
      BLOB_RESOLVER, LONGVARBINARY_RESOLVER, VARBINARY_RESOLVER,
      IMAGE_RESOLVER, VARBINARY_BIT_DATA_RESOLVER, BINARY_RESOLVER
    )
    mappings["LONGBLOB"] = listOf(
      BLOB_RESOLVER, LONGVARBINARY_RESOLVER, VARBINARY_RESOLVER,
      BYTEA_RESOLVER, IMAGE_RESOLVER, VARBINARY_BIT_DATA_RESOLVER, BINARY_RESOLVER
    )
    mappings["OID"] = mappings["LONGBLOB"]!!
    mappings["MEDIUMBLOB"] = mappings["LONGBLOB"]!!
    mappings["SMALLBLOB"] = mappings["LONGBLOB"]!!
    mappings["TINYBLOB"] = mappings["LONGBLOB"]!!
    mappings["BINARY LARGE OBJECT"] = mappings["LONGBLOB"]!!
    mappings["BINARY"] = listOf(
      RAW_RESOLVER, BINARY_RESOLVER,
      BLOB_RESOLVER, LONGVARBINARY_RESOLVER, VARBINARY_RESOLVER, BYTEA_RESOLVER, IMAGE_RESOLVER, VARBINARY_BIT_DATA_RESOLVER
    )

    mappings["DOUBLE PRECISION"] = listOf(DOUBLE_RESOLVER)
    mappings["DOUBLE"] = listOf(DOUBLEPRECISION_RESOLVER)
    mappings["NUMBER"] = listOf(DECIMAL_RESOLVER, BIGINT_RESOLVER)
    mappings["NUMERIC"] = listOf(DECIMAL_RESOLVER)

    mappings["BIGSERIAL"] = listOf(BIGINT_RESOLVER)
    mappings["SMALLSERIAL"] = listOf(SMALLINT_RESOLVER, INTEGER_RESOLVER)
    mappings["TINYSERIAL"] = listOf(TINYINT_RESOLVER, INTEGER_RESOLVER)
    mappings["MEDIUMINT UNSIGNED"] = listOf(INTEGER_RESOLVER)
    mappings["SMALLINT UNSIGNED"] = listOf(SMALLINT_RESOLVER, INTEGER_RESOLVER)
    mappings["TINYINT UNSIGNED"] = listOf(SMALLINT_RESOLVER, INTEGER_RESOLVER)
    mappings["MEDIUMINT"] = listOf(INTEGER_RESOLVER)
    mappings["SMALLINT"] = listOf(INTEGER_RESOLVER)
    mappings["TINYINT"] = listOf(SMALLINT_RESOLVER, INTEGER_RESOLVER)
    mappings["YEAR"] = listOf(SMALLINT_RESOLVER, INTEGER_RESOLVER)

    mappings["INTEGER UNSIGNED"] = listOf(BIGINT_RESOLVER)
    mappings["INT UNSIGNED"] = listOf(BIGINT_RESOLVER)
    mappings["INT"] = listOf(BIGINT_RESOLVER)
    mappings["MONEY"] = listOf(NUMERIC_RESOLVER)
    mappings["SMALL MONEY"] = listOf(NUMERIC_RESOLVER)
    mappings["SMALL DATETIME"] = listOf(DATE_RESOLVER)

    mappings["BOOLEAN"] = listOf(BOOLEAN_RESOLVER, BIT_RESOLVER)
    mappings["BIT"] = listOf(BOOLEAN_RESOLVER)
    mappings["RAW"] = listOf(BIT_RESOLVER)
  }

  /**
   * Resolve column type definition for given column and database type by lookup in specified matrix of [ColumnTypeDefinitionResolver]s.
   */
  override fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition? {
    val resolvers = mappings[type.typeName] ?: listOf()

    return resolvers.asSequence().map { it.resolve(type) }.firstOrNull { it != null }
  }
}

private val BLOB_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BLOB", BLOB)) }
private val IMAGE_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "IMAGE", BLOB)) }
private val BYTEA_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BYTEA", BINARY)) }
private val VARBINARY_BIT_DATA_RESOLVER =
  ColumnTypeDefinitionResolver {
    LookupPreciseMatchResolver.resolve(
      ColumnTypeDefinition(
        it,
        "LONG VARCHAR FOR BIT DATA",
        LONGVARBINARY
      )
    )
  }
private val TEXT_RESOLVER =
  ColumnTypeDefinitionResolver {
    LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "TEXT", LONGVARCHAR))
      ?: LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "TEXT", VARCHAR))
  }
private val LONGTEXT_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "LONGTEXT", LONGVARCHAR)) }
private val CLOB_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "CLOB", CLOB)) }
private val BINARY_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BINARY", BINARY)) }
private val VARBINARY_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "VARBINARY", VARBINARY)) }
private val LONGVARBINARY_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "LONG VARBINARY", LONGVARBINARY)) }
private val INTEGER_RESOLVER =
  ColumnTypeDefinitionResolver {
    LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "INTEGER", INTEGER))
      ?:LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "INT4", INTEGER))
  }
private val SMALLINT_RESOLVER =
  ColumnTypeDefinitionResolver {
    LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "SMALLINT", SMALLINT))
      ?:LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "INT2", SMALLINT))
  }
private val TINYINT_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "TINYINT", TINYINT))
    ?:LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "INT2", SMALLINT))
  }
private val BIGINT_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BIGINT", BIGINT)) }
private val DECIMAL_RESOLVER = ColumnTypeDefinitionResolver {
  LookupPreciseMatchResolver.resolve(
    ColumnTypeDefinition(it.sourceColumn, it.targetDatabase, "DECIMAL", DECIMAL, true, 31, 5)
  )
}
private val DOUBLEPRECISION_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "DOUBLE PRECISION", DOUBLE)) }
private val DOUBLE_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "DOUBLE", DOUBLE)) }
private val NUMERIC_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "NUMERIC", NUMERIC)) }
private val VARCHAR_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "VARCHAR", VARCHAR)) }
private val LONGVARCHAR_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "LONG VARCHAR", LONGVARCHAR)) }
private val CHAR_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "CHAR", CHAR)) }
private val DATE_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "DATE", DATE)) }
private val BOOLEAN_RESOLVER =
  ColumnTypeDefinitionResolver {
    LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BOOLEAN", BOOLEAN))
      ?: LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BOOL", BOOLEAN))
      ?: LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BOOLEAN", BIT))
      ?: LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BOOL", BIT))
  }
private val BIT_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "BIT", BIT)) }
private val RAW_RESOLVER =
  ColumnTypeDefinitionResolver { LookupPreciseMatchResolver.resolve(ColumnTypeDefinition(it, "RAW", BINARY)) }
