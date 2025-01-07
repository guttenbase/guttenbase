package io.github.guttenbase.tools

import io.github.guttenbase.utils.Util.abbreviate
import io.github.guttenbase.utils.Util.forEach
import io.github.guttenbase.utils.Util.mormalizeNegativeScale
import io.github.guttenbase.utils.Util.nextPowerOf2
import io.github.guttenbase.utils.Util.toHex
import io.github.guttenbase.utils.Util.trim
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class UtilTest {
  @Test
  fun `Next power of 2`() {
    assertThat((-127).nextPowerOf2()).isEqualTo(128)
    assertThat(255.nextPowerOf2()).isEqualTo(256)
    assertThat((-1000).nextPowerOf2()).isEqualTo(1024)

    assertThat((-127).mormalizeNegativeScale()).isEqualTo(8)
  }

  @Test
  fun `Hexa-decimal bytes`() {
    val bytes = byteArrayOf(SPACE, A)

    assertEquals("20", bytes[0].toHex())
    assertEquals("2041", bytes.toHex())
  }

  @Test
  fun `For each byte`() {
    val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
    for (i in 0 until DEFAULT_BUFFER_SIZE) {
      bytes[i] = if (i % 2 == 0) SPACE else A
    }

    var count = 0
    ByteArrayInputStream(bytes).forEach {
      val ch = if (count++ % 2 == 0) SPACE else A
      assertEquals(ch, it)
    }
  }

  @Test
  fun `String helpers`() {
    assertThat(trim(null)).isEqualTo("")
    assertThat(trim("")).isEqualTo("")
    assertThat(trim("Jens ")).isEqualTo("Jens")

    assertThat("More than 15 characters".abbreviate(15)).isEqualTo("More than 15...")
  }
}

private const val SPACE = ' '.code.toByte()
private const val A = 'A'.code.toByte()
