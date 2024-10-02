package io.github.guttenbase.io.github.guttenbase.tools

import java.io.RandomAccessFile
import java.nio.file.Path
import java.nio.file.spi.FileTypeDetector
import java.util.zip.GZIPInputStream

class GZIPFileDetector : FileTypeDetector() {
  override fun probeContentType(path: Path): String? {
    val magic = RandomAccessFile(path.toFile(), "r").use { raf -> raf.read() and 0xff or ((raf.read() shl 8) and 0xff00) }

    return if (magic == GZIPInputStream.GZIP_MAGIC) "application/x-gzip-compressed" else null
  }
}