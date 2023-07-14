package io.github.guttenbase.mapping

/**
 * Determine list of used column type resolvers.
 *
 *
 *  &copy; 2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface ColumnTypeResolverList {
  fun getColumnTypeResolvers(): List<ColumnTypeResolver>
}