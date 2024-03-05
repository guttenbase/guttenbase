package io.github.guttenbase.connector.impl

import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.crypto.*
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.system.exitProcess

/**
 * Encrypt/Decrypt properties file with given password
 */
class PropertiesEncryptionTool
@JvmOverloads
constructor(
  private val sourceProperties: Properties,
  private val salt: String = "guttenbase",
  private val intializationVector: ByteArray = ByteArray(16).apply { fill(42) },
  private val keyAlgorithm: String = "PBKDF2WithHmacSHA256",
  private val pkcs5Algorithm: String = "AES/CBC/PKCS5Padding"
) {
  @JvmOverloads
  constructor(
    inputStream: InputStream,
    salt: String = "guttenbase",
    intializationVector: ByteArray = ByteArray(16).apply { fill(42) },
    keyAlgorithm: String = "PBKDF2WithHmacSHA256",
    pkcs5Algorithm: String = "AES/CBC/PKCS5Padding"
  ) : this(Properties(), salt, intializationVector, keyAlgorithm, pkcs5Algorithm) {
    inputStream.use { sourceProperties.load(inputStream) }
  }

  constructor(source: File) : this(source.inputStream())


  @JvmOverloads
  fun encrypt(password: String, outputStream: OutputStream? = null): Properties {
    val key = getKeyFromPassword(password)
    val targetProperties = Properties()

    for (entry in sourceProperties.entries) {
      targetProperties.setProperty(entry.key.toString(), encrypt(entry.value.toString(), key))
    }

    outputStream?.use {
      targetProperties.store(outputStream, "Encrypted DB properties")
    }

    return targetProperties
  }

  fun decrypt(password: String): Properties {
    val key = getKeyFromPassword(password)
    val targetProperties = Properties()

    for (entry in sourceProperties.entries) {
      targetProperties.setProperty(entry.key.toString(), decrypt(entry.value.toString(), key))
    }

    return targetProperties
  }

  private fun encrypt(input: String, key: SecretKey): String {
    val cipher = Cipher.getInstance(pkcs5Algorithm)
    cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(intializationVector))
    val cipherText = cipher.doFinal(input.toByteArray())

    return Base64.getEncoder().encodeToString(cipherText)
  }

  private fun decrypt(input: String, key: SecretKey): String {
    val cipher = Cipher.getInstance(pkcs5Algorithm)
    cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(intializationVector))

    val cipherText = Base64.getDecoder().decode(input)
    val decoded = cipher.doFinal(cipherText)

    return String(decoded)
  }

  private fun getKeyFromPassword(password: String): SecretKey {
    val factory = SecretKeyFactory.getInstance(keyAlgorithm)
    val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 65536, 256)

    return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
  }

  companion object {
    /**
     * java -cp target/classes:$HOME/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib/1.9.22/kotlin-stdlib-1.9.22.jar io.github.guttenbase.connector.impl.PropertiesEncryptionTool /Users/mdahm/development/local/projects/GuttenBase/new/guttenbase/src/test/resources/hsqldb.properties ./encrypted.properties
     */
    @JvmStatic
    fun main(args: Array<String>) {
      if (args.size < 2) {
        System.err.println("Usage: java ... ${PropertiesEncryptionTool::class.qualifiedName} db.properties encrypted.properties")

        exitProcess(-1)
      }

      val console = System.console() ?: throw RuntimeException("Must not be started from an IDE")
      val password = String(console.readPassword("Please enter password: "))

      PropertiesEncryptionTool(File(args[0])).encrypt(password, File(args[1]).outputStream())
    }
  }
}