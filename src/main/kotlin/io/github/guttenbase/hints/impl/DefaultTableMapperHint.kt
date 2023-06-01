package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultTableMapper
import io.github.guttenbase.hints.TableMapperHint
import io.github.guttenbase.mapping.TableMapper


/**
 * By default return table with same name ignoring case.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultTableMapperHint : TableMapperHint() {
 override val value: TableMapper
    get() = DefaultTableMapper()
}
