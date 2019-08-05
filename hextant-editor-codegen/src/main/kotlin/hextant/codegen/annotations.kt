/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS

private const val DEFAULT = "<default>"

internal fun String.orDefault(default: () -> String) = if (this == DEFAULT) default() else this

@Retention(SOURCE)
@Target(CLASS)
annotation class Token(val pkg: String = DEFAULT, val name: String = DEFAULT)