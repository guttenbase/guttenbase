package io.github.guttenbase.utils

import io.github.guttenbase.utils.Util.isWindows
import java.io.File
import java.io.IOException
import java.net.URLDecoder

/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ResourceUtil {
  @Throws(IOException::class)
  fun getResourceInfo(clazz: Class<*>): ResourceInfo {
    val pathToClass = '/'.toString() + clazz.name.replace('.', '/') + ".class"
    val resource = clazz.getResource(pathToClass) ?: throw IllegalStateException("Class file not found ${clazz.name}")
    var path = resource.path
    val protocol = resource.protocol

    // file:/Users/mdahm/projects/workspace/GuttenBase/target/test-classes/.../utils/ResourceUtilTest.class
    if ("file".equals(protocol, ignoreCase = true)) {
      path = resource.path.substring(0, path.length - pathToClass.length)
    } else if ("jar".equals(protocol, ignoreCase = true)) {
      path = resource.path.substring(0, path.length - (pathToClass.length + 1))
      if (path.startsWith("file:")) {
        path = path.substring(5)
      }
    } else {
      throw IOException("Cannot handle protocol $protocol while reading classes")
    }

    if (isWindows) {
      if (path.startsWith("/")) {
        path = path.substring(1)
      }

      path = path.replace('\\', '/') // avoid ugly DOS path names
    }
    path = URLDecoder.decode(path, "UTF-8")
    return ResourceInfo(protocol, File(path), pathToClass)
  }

  data class ResourceInfo(val protocol: String, val jarFileOrFolder: File, val pathToClass: String) {
    val isJarFile: Boolean
      get() = jarFileOrFolder.canRead() && jarFileOrFolder.isFile

    override fun toString(): String {
      return "ResourceInfo{" +
          "_protocol='" + protocol + '\'' +
          ", _jarFileOrFolder=" + jarFileOrFolder +
          ", _pathToClass='" + pathToClass + '\'' +
          '}'
    }
  }
}
