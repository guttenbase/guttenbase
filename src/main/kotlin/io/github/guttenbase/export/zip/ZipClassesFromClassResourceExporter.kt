package io.github.guttenbase.export.zip


import io.github.guttenbase.utils.ResourceUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Copy all classes and data that can be found relative to the given class resource to the generated JAR/ZIP.
 *
 *
 * This allows us to create a self-contained executable JAR with a user defined startup class.
 *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipClassesFromClassResourceExporter(zipOutputStream: ZipOutputStream) : ZipResourceExporter(zipOutputStream) {
  /**
   * Copy all classes and data that can be found relative to the given class resource to the generated JAR/ZIP.
   *
   * We support classes read from file system or JAR.
   */
  @Throws(IOException::class)
  fun copyClassesToZip(startupClass: Class<*>) {
    val resourceInfo = ResourceUtil().getResourceInfo(startupClass)

    if (resourceInfo.isJarFile) {
      copyClassesFromJar(resourceInfo.jarFileOrFolder)
    } else {
      copyClassesFromFilesystem(resourceInfo.jarFileOrFolder, resourceInfo.jarFileOrFolder.getPath())
    }
  }

  @Throws(IOException::class)
  private fun copyClassesFromFilesystem(dir: File, rootPath: String) {
    for (file in dir.listFiles()!!) {
      addFileToJar(file, rootPath)
    }
  }

  @Throws(IOException::class)
  private fun addFileToJar(path: File, rootPath: String) {
    if (!path.isFile) {
      copyClassesFromFilesystem(path, rootPath)
    } else {
      val name: String = path.path.substring(rootPath.length + 1)
      val inputStream: InputStream = FileInputStream(path)

      addEntry(name, inputStream)
    }
  }

  @Throws(IOException::class)
  private fun copyClassesFromJar(path: File) {
    ZipFile(path).use {
      val entries: Enumeration<out ZipEntry> = it.entries()

      while (entries.hasMoreElements()) {
        val zipEntry: ZipEntry = entries.nextElement()
        val inputStream: InputStream = it.getInputStream(zipEntry)

        addEntry(zipEntry.name, inputStream)
      }
    }
  }
}
