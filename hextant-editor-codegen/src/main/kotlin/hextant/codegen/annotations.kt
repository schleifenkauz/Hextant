/**
 * @author Nikolaus Knop
 */

package hextant.codegen

import hextant.core.Editor
import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER
import kotlin.reflect.KClass

const val DEFAULT = "<default>"

internal sealed class None

@Retention(SOURCE)
@Target(CLASS)
annotation class Token(val classLocation: String = DEFAULT, val subtypeOf: KClass<*> = None::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class Compound(val classLocation: String = DEFAULT, val subtypeOf: KClass<*> = None::class)

@Retention(SOURCE)
@Target(CLASS)
annotation class Alternative(val interfaceLocation: String = DEFAULT)

@Retention(SOURCE)
@Target(CLASS)
annotation class Expandable(
    val delegator: KClass<out ExpanderDelegator<*>>,
    val expanderLocation: String = DEFAULT,
    val subtypeOf: KClass<*> = None::class
)

@Retention(SOURCE)
@Target(CLASS)
annotation class EditableList(val classLocation: String = DEFAULT, val editorCls: KClass<*> = None::class)

@Retention(SOURCE)
@Target(VALUE_PARAMETER, CLASS)
annotation class UseEditor(val cls: KClass<out Editor<*>>)

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