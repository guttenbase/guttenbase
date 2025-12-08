package io.github.guttenbase.meta

import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.defaults.impl.BYTE_ONE
import io.github.guttenbase.exceptions.UnhandledColumnTypeException
import io.github.guttenbase.meta.DatabaseType.POSTGRESQL
import io.github.guttenbase.utils.Util
import kotlinx.serialization.Serializable
import java.io.Closeable
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.sql.JDBCType.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Define column type and mapping methods. The entries correspond to the available getXXX() methods of a
 * [ResultSet], e.g. getString()
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
@Suppress("MemberVisibilityCanBePrivate", "unused", "RemoveRedundantQualifierName")
@Serializable
enum class ColumnType(
  /**
   * @return JDBC types corresponding to this type
   */
  val jdbcTypes: List<JDBCType>,
  /**
   * @return classes handled by this type
   */
  val columnClasses: List<Class<*>>
) {
  CLASS_UNKNOWN(OTHER, Void::class.java),

  CLASS_STRING(STRING_TYPES, String::class.java, Char::class.java, Character::class.java),

  CLASS_BIGDECIMAL(listOf(NUMERIC, DECIMAL, REAL), BigDecimal::class.java),

  CLASS_BLOB(BLOB, Blob::class.java),

  CLASS_CLOB(CLOB, NCLOB, Clob::class.java),

  CLASS_SQLXML(SQLXML, SQLXML::class.java),

  CLASS_OBJECT(JAVA_OBJECT, Any::class.java, java.io.Serializable::class.java),

  CLASS_ARRAY(ARRAY, Array::class.java, java.sql.Array::class.java),

  CLASS_DATE(DATE, Date::class.java, LocalDate::class.java),

  CLASS_TIMESTAMP(TIMESTAMP, Timestamp::class.java, LocalDateTime::class.java),

  CLASS_TIME(TIME, Time::class.java, LocalTime::class.java),

  CLASS_INTEGER(INTEGER, Int::class.java, Integer::class.java),

  CLASS_BOOLEAN(BOOLEAN_TYPES, Boolean::class.java, java.lang.Boolean::class.java),

  CLASS_LONG(BIGINT, Long::class.java, BigInteger::class.java, java.lang.Long::class.java),

  CLASS_DOUBLE(DOUBLE, Double::class.java, java.lang.Double::class.java),

  CLASS_FLOAT(FLOAT, Float::class.java, java.lang.Float::class.java),

  CLASS_BYTE(listOf(TINYINT, BIT), Byte::class.javaPrimitiveType!!, java.lang.Byte::class.java),

  CLASS_BYTES(BINARY_TYPES, Util.ByteArrayClass),

  CLASS_SHORT(SMALLINT, Short::class.java, java.lang.Short::class.java);

  constructor(jdbcType1: JDBCType, columnClass1: Class<*>) : this(listOf(jdbcType1), listOf(columnClass1))

  constructor(
    jdbcType1: JDBCType, columnClass1: Class<*>, columnClass2: Class<*>, columnClass3: Class<*>
  ) : this(listOf(jdbcType1), listOf(columnClass1, columnClass2, columnClass3))

  constructor(
    jdbcType1: JDBCType, columnClass1: Class<*>, columnClass2: Class<*>
  ) : this(listOf(jdbcType1), listOf(columnClass1, columnClass2))

  constructor(
    jdbcTypes: List<JDBCType>,
    columnClass1: Class<*>, columnClass2: Class<*>, columnClass3: Class<*>
  ) : this(jdbcTypes, listOf(columnClass1, columnClass2, columnClass3))

  constructor(jdbcTypes: List<JDBCType>, columnClass1: Class<*>) : this(jdbcTypes, listOf(columnClass1))

  constructor(jdbcTypes: List<JDBCType>, columnClass1: Class<*>, columnClass2: Class<*>)
      : this(jdbcTypes, listOf(columnClass1, columnClass2))

  constructor(jdbcType1: JDBCType, jdbcType2: JDBCType, columnClass1: Class<*>) : this(
    listOf(jdbcType1, jdbcType2), listOf(columnClass1)
  )

  /**
   * Get value from [ResultSet]
   */
  fun getValue(resultSet: ResultSet, columnIndex: Int, column: ColumnMetaData): Any? {
    val result = getValueFromResultset(resultSet, columnIndex, column)

    return if (resultSet.wasNull()) {
      null
    } else {
      result
    }
  }

  private fun getValueFromResultset(resultSet: ResultSet, columnIndex: Int, column: ColumnMetaData): Any? = when (this) {
    CLASS_STRING -> resultSet.getString(columnIndex)
    CLASS_DOUBLE -> resultSet.getDouble(columnIndex)
    CLASS_INTEGER -> resultSet.getInt(columnIndex)
    CLASS_LONG -> resultSet.getLong(columnIndex)
    CLASS_BLOB -> resultSet.getBlob(columnIndex)
    CLASS_CLOB -> resultSet.getClob(columnIndex)
    CLASS_SQLXML -> resultSet.getSQLXML(columnIndex)
    CLASS_FLOAT -> resultSet.getFloat(columnIndex)
    CLASS_BOOLEAN -> resultSet.getBoolean(columnIndex)
    CLASS_BIGDECIMAL -> resultSet.getBigDecimal(columnIndex)
    CLASS_TIMESTAMP -> resultSet.getTimestamp(columnIndex)
    CLASS_DATE -> resultSet.getDate(columnIndex)
    CLASS_SHORT -> resultSet.getShort(columnIndex)
    CLASS_TIME -> resultSet.getTime(columnIndex)

    CLASS_UNKNOWN, // OTHER data -> let the JDBC driver decide
    CLASS_OBJECT -> {
      resultSet.getObject(columnIndex)
    }

    CLASS_ARRAY -> resultSet.getArray(columnIndex)

    CLASS_BYTE -> {
      if (column.databaseType == POSTGRESQL && column.columnTypeName == "BOOL") {
        resultSet.getBoolean(columnIndex)
      } else {
        resultSet.getByte(columnIndex)
      }
    }

    CLASS_BYTES -> resultSet.getBytes(columnIndex)
  }

  /**
   * Set value in [PreparedStatement]
   */
  fun setValue(
    insertStatement: PreparedStatement, columnIndex: Int, column: ColumnMetaData, data: Any?
  ): Closeable? {
    return if (data == null) {
      insertStatement.setNull(columnIndex, column.columnType)
      null
    } else {
      setStatementValue(insertStatement, column, columnIndex, data)
    }
  }

  private fun setStatementValue(
    insertStatement: PreparedStatement, column: ColumnMetaData, columnIndex: Int, data: Any
  ): Closeable? {
    var result: Closeable? = null

    when (this) {
      CLASS_STRING -> when (data) {
        is String -> insertStatement.setString(columnIndex, data)
        is Char -> insertStatement.setString(columnIndex, data.toString())
        is Clob -> {
          if (column.databaseType == POSTGRESQL) { // Workaround for PostgreSQL
            data.characterStream.use {
              insertStatement.setString(columnIndex, it.readText())
            }
          } else {
            result = data.characterStream
            insertStatement.setClob(columnIndex, data.characterStream)
          }
        }

        else -> throw IllegalStateException("This is no string data: ${data.javaClass}")
      }

      CLASS_INTEGER -> insertStatement.setInt(columnIndex, (data as Int))
      CLASS_LONG -> insertStatement.setLong(columnIndex, (data as Long))
      CLASS_DOUBLE -> insertStatement.setDouble(columnIndex, (data as Double))
      CLASS_BLOB -> {
        val inputStream = (data as Blob).binaryStream
        result = inputStream
        insertStatement.setBlob(columnIndex, inputStream)
      }

      CLASS_CLOB -> {
        val characterStream = (data as Clob).characterStream
        result = characterStream
        insertStatement.setClob(columnIndex, characterStream)
      }

      CLASS_SQLXML -> {
        val inputStream: InputStream = (data as SQLXML).binaryStream
        result = inputStream
        insertStatement.setBlob(columnIndex, inputStream)
      }

      CLASS_BOOLEAN -> insertStatement.setBoolean(columnIndex, (data as Boolean))
      CLASS_BIGDECIMAL -> insertStatement.setBigDecimal(columnIndex, data as BigDecimal)

      CLASS_TIMESTAMP -> insertStatement.setTimestamp(columnIndex, data as Timestamp)
      CLASS_DATE -> {
        if (column.databaseType == DatabaseType.MYSQL && column.columnTypeName == "YEAR") {
          insertStatement.setInt(columnIndex, (data as Date).toLocalDate().year)
        } else {
          insertStatement.setDate(columnIndex, data as Date)
        }
      }

      CLASS_FLOAT -> insertStatement.setFloat(columnIndex, (data as Float))
      CLASS_SHORT -> insertStatement.setShort(columnIndex, (data as Short))
      CLASS_TIME -> insertStatement.setTime(columnIndex, data as Time)

      CLASS_UNKNOWN, CLASS_OBJECT -> {
        insertStatement.setObject(columnIndex, data)
      }

      CLASS_ARRAY -> insertStatement.setArray(columnIndex, data as java.sql.Array)

      CLASS_BYTE -> if (column.databaseType == POSTGRESQL) {
        if (column.columnTypeName == "BOOL") {
          val value = data as? Boolean ?: (data == BYTE_ONE) // See object ToBitMapper in DefaultColumnDataMapperProvider
          insertStatement.setBoolean(columnIndex, value)
        } else {
          // Postgres has a weird concept of setting a BIT value
          insertStatement.setInt(columnIndex, (data as Byte).toInt())
        }
      } else {
        insertStatement.setByte(columnIndex, data as Byte)
      }

      CLASS_BYTES -> when (data) {
        is ByteArray -> insertStatement.setBytes(columnIndex, data)
        is Blob -> {
          if (column.databaseType == POSTGRESQL) { // Workaround for PostgreSQL
            data.binaryStream.use {
              insertStatement.setBytes(columnIndex, it.readAllBytes())
            }
          } else {
            result = data.binaryStream
            insertStatement.setBlob(columnIndex, data.binaryStream)
          }
        }

        else -> throw IllegalStateException("This is no byte array: ${data.javaClass}")
      }
    }

    return result
  }

  private fun driverSupportsJavaTimeAPI(databaseMetaData: DatabaseMetaData) = when {
    // We know that this driver is too old
    databaseMetaData.metaData.driverName.startsWith("jTDS Type 4 JDBC Driver") -> false
    else -> true
  }

  val isNumber get() = Number::class.java.isAssignableFrom(columnClasses[0])

  fun isDate() = jdbcTypes[0].isDateType()

  companion object {
    private val COLUMN_TYPE_BY_CLASS: Map<Class<*>, ColumnType> by lazy {
      entries.map { ct -> ct.columnClasses.map { it to ct } }.flatten().toMap()
    }

    private val COLUMN_TYPE_BY_JDBC_TYPE: Map<JDBCType, ColumnType> by lazy {
      entries.map { ct -> ct.jdbcTypes.map { it to ct } }.flatten().toMap()
    }

    /**
     * Map class to [ColumnType].
     */
    @JvmStatic
    fun valueOf(columnClass: Class<*>) = COLUMN_TYPE_BY_CLASS[columnClass]
      ?: throw UnhandledColumnTypeException("Unhandled column class ${columnClass.name}")

    /**
     * Map type to [ColumnType].
     */
    @JvmStatic
    fun valueOf(type: JDBCType) = COLUMN_TYPE_BY_JDBC_TYPE[type]
      ?: throw UnhandledColumnTypeException("Unhandled type $type")

    @JvmStatic
    fun valueOfClassName(className: String) = valueOf(forName(className))

    /**
     * Check if class can be mapped to [ColumnType].
     */
    @JvmStatic
    fun Class<*>.isSupportedColumnType() = COLUMN_TYPE_BY_CLASS.containsKey(this)

    /**
     * Check if class can be mapped to [ColumnType].
     */
    @JvmStatic
    fun JDBCType.isSupported() = COLUMN_TYPE_BY_JDBC_TYPE.containsKey(this)

    /**
     * Check if class can be mapped to [ColumnType].
     */
    @JvmStatic
    fun String.isSupportedColumnType() = forName(this).isSupportedColumnType()

    private fun forName(className: String): Class<*> {
      return if ("byte[]" == className) { // Oracle-Bug
        Util.ByteArrayClass
      } else {
        try {
          Class.forName(className)
        } catch (e: ClassNotFoundException) {
          throw GuttenBaseException("Class not found: $className", e)
        }
      }
    }
  }
}
