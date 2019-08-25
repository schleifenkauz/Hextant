/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.reflect.KClass

const val DEFAULT = "<default>"

sealed class NotASubtypeOfAnything

@Retention(SOURCE)
@Target(CLASS)
annotation class Token(val classLocation: String = DEFAULT, val subtypeOf: KClass<*> = NotASubtypeOfAnything::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class Compound(val classLocation: String = DEFAULT, val subtypeOf: KClass<*> = NotASubtypeOfAnything::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class Alternative(val interfaceLocation: String = DEFAULT)

@Retention(SOURCE)
@Target(CLASS)
annotation class Expandable(
    val delegator: KClass<out ExpanderDelegator<*>>,
    val expanderLocation: String = DEFAULT,
    val subtypeOf: KClass<*> = NotASubtypeOfAnything::class
)

@Retention(SOURCE)
@Target(CLASS)
annotation class EditableList(val classLocation: String = DEFAULT)

internal val Annotation.qualifiedEditorClassName: String?
    get() = when (this) {
        is Token        -> this.classLocation.takeIf { it != DEFAULT }
        is Compound     -> this.classLocation.takeIf { it != DEFAULT }
        is Alternative  -> this.interfaceLocation.takeIf { it != DEFAULT }
        is Expandable   -> this.expanderLocation.takeIf { it != DEFAULT }
        is EditableList -> this.classLocation.takeIf { it != DEFAULT }
        else            -> throw AssertionError()
    }

internal val Annotation.subtypeOf: KClass<*>
    get() = when (this) {
        is Token      -> this.subtypeOf
        is Compound   -> this.subtypeOf
        is Expandable -> this.subtypeOf
        else          -> throw AssertionError()
    }