package io.github.guttenbase.meta

import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.exceptions.UnhandledColumnTypeException
import io.github.guttenbase.utils.Util
import io.github.guttenbase.utils.Util.toDate
import io.github.guttenbase.utils.Util.toSQLDate
import java.io.Closeable
import java.io.InputStream
import java.io.Serializable
import java.math.BigDecimal
import java.math.BigInteger
import java.sql.*
import java.sql.Date
import java.time.LocalDateTime
import java.util.*

/**
 * Define column type and mapping methods
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
enum class ColumnType(vararg classes: Class<*>) {
  CLASS_UNKNOWN(Void::class.java),

  //
  CLASS_STRING(String::class.java, Char::class.java, java.lang.Character::class.java),

  //
  CLASS_BIGDECIMAL(BigDecimal::class.java),

  //
  CLASS_BLOB(Blob::class.java),

  //
  CLASS_CLOB(Clob::class.java),

  //
  CLASS_SQLXML(SQLXML::class.java),

  //
  CLASS_OBJECT(Any::class.java, Serializable::class.java, Util.ByteArrayClass),

  //
  CLASS_DATE(Date::class.java),

  //
  CLASS_TIMESTAMP(Timestamp::class.java),

  //
  CLASS_DATETIME(LocalDateTime::class.java),

  //
  CLASS_TIME(Time::class.java),

  //
  CLASS_INTEGER(Int::class.java, java.lang.Integer::class.java),

  //
  CLASS_BOOLEAN(Boolean::class.java, java.lang.Boolean::class.java),

  //
  CLASS_LONG(Long::class.java, BigInteger::class.java, java.lang.Long::class.java),

  //
  CLASS_DOUBLE(Double::class.java, java.lang.Double::class.java),

  //
  CLASS_FLOAT(Float::class.java, java.lang.Float::class.java),

  //
  CLASS_BYTE(Byte::class.javaPrimitiveType!!, java.lang.Byte::class.java),

  //
  CLASS_BYTES(ByteArray::class.java),

  //
  CLASS_SHORT(Short::class.java, java.lang.Short::class.java);

  /**
   * @return classes handled by this type
   */
  val columnClasses: List<Class<*>> = listOf(*classes)

  /**
   * Get value from [ResultSet]
   */
  @Throws(SQLException::class)
  fun getValue(resultSet: ResultSet, columnIndex: Int): Any? {
    val result = getValueFromResultset(resultSet, columnIndex)

    return if (resultSet.wasNull()) {
      null
    } else {
      result
    }
  }

  @Throws(SQLException::class)
  private fun getValueFromResultset(resultSet: ResultSet, columnIndex: Int): Any? {
    return when (this) {
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
      CLASS_DATETIME -> resultSet.getObject(columnIndex, LocalDateTime::class.java)
      CLASS_OBJECT -> resultSet.getObject(columnIndex)
      CLASS_BYTE -> resultSet.getByte(columnIndex)
      CLASS_BYTES -> resultSet.getBytes(columnIndex)
      else -> throw UnhandledColumnTypeException("Unhandled column type ($this)")
    }
  }

  /**
   * Set value in [PreparedStatement]
   */
  @Throws(SQLException::class)
  fun setValue(
    insertStatement: PreparedStatement, columnIndex: Int, databaseMetaData: DatabaseMetaData,
    sqlType: Int, data: Any?
  ): Closeable? {
    return if (data == null) {
      insertStatement.setNull(columnIndex, sqlType)
      null
    } else {
      setStatementValue(insertStatement, columnIndex, databaseMetaData, data)
    }
  }

  @Throws(SQLException::class)
  private fun setStatementValue(
    insertStatement: PreparedStatement, columnIndex: Int, databaseMetaData: DatabaseMetaData, data: Any
  ): Closeable? {
    var result: Closeable? = null

    when (this) {
      CLASS_STRING -> insertStatement.setString(columnIndex, convertToString(data))
      CLASS_INTEGER -> insertStatement.setInt(columnIndex, (data as Int))
      CLASS_LONG -> insertStatement.setLong(columnIndex, (data as Long))
      CLASS_DOUBLE -> insertStatement.setDouble(columnIndex, (data as Double))
      CLASS_BLOB -> {
        val inputStream = (data as Blob).binaryStream
        result = inputStream
        insertStatement.setBlob(columnIndex, inputStream)
      }
      CLASS_CLOB ->  {
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
      CLASS_DATE -> insertStatement.setDate(columnIndex, data as Date)
      CLASS_FLOAT -> insertStatement.setFloat(columnIndex, (data as Float))
      CLASS_SHORT -> insertStatement.setShort(columnIndex, (data as Short))
      CLASS_TIME -> insertStatement.setTime(columnIndex, data as Time)
      CLASS_DATETIME -> if (driverSupportsJavaTimeAPI(databaseMetaData)) {
        // Let the driver choose what it is
        insertStatement.setObject(columnIndex, data)
      } else {
        // For older drivers not supporting LocalDateTime directly
        val sqlDate = data.toDate().toSQLDate()

        insertStatement.setDate(columnIndex, sqlDate)
      }

      CLASS_OBJECT -> insertStatement.setObject(columnIndex, data)
      CLASS_BYTE -> insertStatement.setByte(columnIndex, data as Byte)
      CLASS_BYTES -> insertStatement.setBytes(columnIndex, data as ByteArray)
      else -> throw UnhandledColumnTypeException("Unhandled column type ($this)")
    }

    return result
  }

  private fun convertToString(data: Any): String = when (data) {
    is String -> data
    is Char -> data.toString()
    else -> throw IllegalStateException("Whart is this: $data")
  }

  private fun driverSupportsJavaTimeAPI(databaseMetaData: DatabaseMetaData) = when {
    // We know that this driver is too old
    databaseMetaData.databaseMetaData.driverName.startsWith("jTDS Type 4 JDBC Driver") -> false
    else -> true
  }

  val isNumber: Boolean
    get() = Number::class.java.isAssignableFrom(columnClasses[0])

  fun isDate() = when (this) {
    CLASS_TIME, CLASS_TIMESTAMP, CLASS_DATETIME, CLASS_DATE -> true
    else -> false
  }

  companion object {
    private val COLUMN_TYPES: Map<Class<*>, ColumnType> by lazy {
      entries.map { ct -> ct.columnClasses.map { it to ct } }.flatten().toMap()
    }

    /**
     * Map class to [ColumnType].
     */
    fun valueOf(columnClass: Class<*>) = COLUMN_TYPES[columnClass]
      ?: throw UnhandledColumnTypeException("Unhandled column class ${columnClass.name}")

    /**
     * Check if class can be mapped to [ColumnType].
     */
    fun isSupportedClass(columnClass: Class<*>) = COLUMN_TYPES.containsKey(columnClass)

    /**
     * Check if class can be mapped to [ColumnType].
     */
    fun isSupportedClass(className: String) = isSupportedClass(forName(className))

    fun valueForClass(className: String) = valueOf(forName(className))

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
