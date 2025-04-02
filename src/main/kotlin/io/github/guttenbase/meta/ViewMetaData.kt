package io.github.guttenbase.meta

import java.io.Serializable

/**
 * Information about a database <a href="https://www.w3schools.com/sql/sql_view.asp">view</a>.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ViewMetaData : Comparable<ViewMetaData>, Serializable, DatabaseEntityMetaData

