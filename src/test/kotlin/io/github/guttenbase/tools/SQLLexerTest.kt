package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.sql.SQLLexer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SQLLexerTest {
  @Test
  fun `parse single line`() {
    val objectUnderTest = SQLLexer(listOf("DROP TABLE dbo.ClientSystem;"))

    assertThat(objectUnderTest.parse()).isEqualTo(listOf("DROP TABLE dbo.ClientSystem"))
  }

  @Test
  fun `trailing statement ignored`() {
    val objectUnderTest = SQLLexer(listOf("DROP TABLE dbo.ClientSystem;DROP TABLE dbo.jens"))

    assertThat(objectUnderTest.parse()).isEqualTo(listOf("DROP TABLE dbo.ClientSystem"))
  }

  @Test
  fun `parse multiple lines`() {
    val objectUnderTest = SQLLexer(listOf("DROP TABLE dbo.ClientSystem;", "DROP TABLE dbo.jens; -- Hello"))

    assertThat(objectUnderTest.parse()).isEqualTo(listOf("DROP TABLE dbo.ClientSystem", "DROP TABLE dbo.jens"))
  }

  @Test
  fun `parse two statements in single lines`() {
    val objectUnderTest = SQLLexer(listOf("DROP TABLE dbo.ClientSystem ; DROP TABLE dbo.jens; -- Hello"))

    assertThat(objectUnderTest.parse()).isEqualTo(listOf("DROP TABLE dbo.ClientSystem", "DROP TABLE dbo.jens"))
  }
}